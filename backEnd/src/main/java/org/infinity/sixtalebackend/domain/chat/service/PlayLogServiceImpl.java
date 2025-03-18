package org.infinity.sixtalebackend.domain.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.infinity.sixtalebackend.domain.character_sheet.domain.CharacterSheet;
import org.infinity.sixtalebackend.domain.character_sheet.repository.CharacterSheetRepository;
import org.infinity.sixtalebackend.domain.chat.domain.PlayChatLog;
import org.infinity.sixtalebackend.domain.chat.domain.PlayWhisperLog;
import org.infinity.sixtalebackend.domain.chat.domain.WaitingChatLog;
import org.infinity.sixtalebackend.domain.chat.domain.WaitingWhisperLog;
import org.infinity.sixtalebackend.domain.chat.dto.*;
import org.infinity.sixtalebackend.domain.chat.repository.PlayChatLogRepository;
import org.infinity.sixtalebackend.domain.chat.repository.PlayWhisperLogRepository;
import org.infinity.sixtalebackend.domain.chat.repository.WaitingChatLogRepository;
import org.infinity.sixtalebackend.domain.chat.repository.WaitingWhisperLogRepository;
import org.infinity.sixtalebackend.domain.member.domain.Member;
import org.infinity.sixtalebackend.domain.member.repository.MemberRepository;
import org.infinity.sixtalebackend.domain.room.domain.PlayMember;
import org.infinity.sixtalebackend.domain.room.repository.PlayMemberRepository;
import org.infinity.sixtalebackend.domain.room.repository.RoomRepository;
import org.infinity.sixtalebackend.infra.redis.service.RedisPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Service
@RequiredArgsConstructor
public class PlayLogServiceImpl implements PlayLogService{
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final PlayChatLogRepository playChatLogRepository;
    private final PlayWhisperLogRepository playWhisperLogRepository;
    private final ChatRoomService chatRoomService;
    private final RedisPublisher redisPublisher;
    private final PlayMemberRepository playMemberRepository;
    private final CharacterSheetRepository characterSheetRepository;
    private final RedisTemplate<String, String> redisTemplate; // RedisTemplate 추가
    private final ObjectMapper objectMapper; // JSON 직렬화를 위한 ObjectMapper 추가

    /**
     * 대기방 채팅, 귓속말 채팅 기능
     * @param chatMessageRequest
     */
    @Transactional
    @Override
    public void sendPlayChatMessage(ChatMessageRequest chatMessageRequest) {

        Member member = findMember(chatMessageRequest.getMemberID());
        findRoom(chatMessageRequest.getRoomID());

        // 회원 닉네임 -> 플레이 멤버 이름으로 변경
        PlayMember playMember = findPlayMember(chatMessageRequest.getMemberID(),chatMessageRequest.getRoomID());
        CharacterSheet characterSheet = findCharacterSheet(playMember.getId());

        // 닉네임 저장
        chatMessageRequest.setNickName(characterSheet.getName());
        chatMessageRequest.setRoomType(RoomType.PLAY);

        if (MessageType.ENTER.equals(chatMessageRequest.getType())) {
            // 채팅 입장 시, 룸 아이디 토픽없으면 토픽 생성 -> pub/sub기능 할 수 있도록 리스너 설정
            chatRoomService.enterChatRoom(chatMessageRequest.getRoomID().toString());
            chatMessageRequest.setContent(chatMessageRequest.getNickName()+ "님이 입장하셨습니다.");
        }
        if(MessageType.WHISPER.equals(chatMessageRequest.getType())){
            handleWhisperMessage(chatMessageRequest.getRoomID(), member, chatMessageRequest);
        }else {
            // 기존 방식
            // handleChatMessage(chatMessageRequest.getRoomID(), member, chatMessageRequest);

            // Redis에 로그 저장
            saveLogToRedis(chatMessageRequest);
        }
        // Websocket에 발행된 메시지를 redis로 발행한다(publish)
        redisPublisher.publish(chatRoomService.getTopic(String.valueOf(chatMessageRequest.getRoomID())), chatMessageRequest);
    }

    private void saveLogToRedis(ChatMessageRequest chatMessageRequest) {
        try {
            String redisKey = "chatLogs:waitingRoom:" + chatMessageRequest.getRoomID();
            String logJson = objectMapper.writeValueAsString(chatMessageRequest); // JSON 변환
            redisTemplate.opsForList().leftPush(redisKey, logJson); // Redis List에 저장
        } catch (Exception e) {
            throw new RuntimeException("Failed to save log to Redis", e);
        }
    }

    /**
     * 전체 전송 로그 저장
     * @param roomID
     * @param member
     * @param chatMessageRequest
     */
    private void handleChatMessage(Long roomID, Member member, ChatMessageRequest chatMessageRequest) {
        PlayChatLog playChatLog = PlayChatLog.builder()
                .roomID(roomID)
                .memberID(member.getId())
                .content(chatMessageRequest.getContent())
                .createdAt(LocalDateTime.now())
                .build();

        playChatLogRepository.save(playChatLog);
    }

    /**
     * 전체 전송 로그 저장(페이지네이션)
     * @param roomID
     * @param pageable
     * @return
     */
    @Override
    public ChatMessageListResponse getPlayChatLogList(Long roomID, Pageable pageable) {
        // 방 유효성 검사
        findRoom(roomID);

        // 페이지네이션과 정렬을 포함한 Pageable 객체 생성
        Page<PlayChatLog> playChatLogs = playChatLogRepository.findByRoomID(roomID, pageable);

        // 날짜/시간 포맷 설정
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Entity - Dto 수동 변환 방법
        //DTO로 변환
        List<ChatMessageResponse> chatMessageResponseList = playChatLogs.getContent().stream()
                .map(c -> {
                    Member member = findMember(c.getMemberID());
                    ChatMessageRequest chatMessageRequest = ChatMessageRequest.builder()
                            .roomID(roomID)
                            .memberID(c.getMemberID())
                            .nickName(member.getNickname())
                            .recipientID(null)
                            .recipientNickName(null)
                            .content(c.getContent())
                            .type(MessageType.CHAT)
                            .roomType(RoomType.PLAY)
                            .createdAt(c.getCreatedAt().format(dateTimeFormatter)).build();

                    return new ChatMessageResponse(chatMessageRequest);
                }).toList();

        return ChatMessageListResponse.builder()
                .chatMessageResponseList(chatMessageResponseList)
                .totalPages(playChatLogs.getTotalPages())
                .totalElements(playChatLogs.getTotalElements()).build();
    }

    /**
     * 플레이방 귓속말 로그 리스트 조회(페이지네이션)
     * @param roomID
     * @param memberID
     * @param pageable
     * @return
     */
    @Override
    public ChatMessageListResponse getPlayChatWisperLogList(Long roomID, Long memberID, Pageable pageable) {
        // 회원 유효 검사
        findMember(memberID);
        // 방 유효성 검사
        findRoom(roomID);

        // 페이지네이션과 정렬을 포함한 Pageable 객체 생성
        Page<PlayWhisperLog> playWhisperLogs = playWhisperLogRepository.findByRoomIDAndRecipientID(roomID,memberID, pageable);

        // 날짜/시간 포맷 설정
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Entity - Dto 수동 변환 방법
        //DTO로 변환
        List<ChatMessageResponse> chatMessageResponseList = playWhisperLogs.getContent().stream()
                .map(c -> {
                    Member member = findMember(c.getMemberID());
                    Member recipient = findMember(c.getRecipientID());
                    ChatMessageRequest chatMessageRequest = ChatMessageRequest.builder()
                            .roomID(roomID)
                            .memberID(c.getMemberID())
                            .nickName(member.getNickname())
                            .recipientID(c.getRecipientID())
                            .recipientNickName(recipient.getNickname())
                            .content(c.getContent())
                            .type(MessageType.WHISPER)
                            .roomType(RoomType.WAITING)
                            .createdAt(c.getCreatedAt().format(dateTimeFormatter)).build();

                    return new ChatMessageResponse(chatMessageRequest);
                }).toList();

        return ChatMessageListResponse.builder()
                .chatMessageResponseList(chatMessageResponseList)
                .totalPages(playWhisperLogs.getTotalPages())
                .totalElements(playWhisperLogs.getTotalElements()).build();
    }

    /**
     * 플레이방 전체 채팅 로그 리스트 조회(페이지네이션)
     * @param roomID
     * @param memberID
     * @param pageable
     * @return
     */
    @Override
    public ChatMessageListResponse getPlayChatAllLogList(Long roomID, Long memberID, Pageable pageable) {
        // 대기방 채팅 로그와 귓속말 로그 모두 조회 (페이지네이션 없이 전체 조회)
        List<ChatMessageResponse> chatMessages = getPlayChatLogList(roomID, Pageable.unpaged()).getChatMessageResponseList();
        List<ChatMessageResponse> whisperMessages = getPlayChatWisperLogList(roomID, memberID, Pageable.unpaged()).getChatMessageResponseList();

        // 두 리스트를 합치고 시간순으로 정렬
        List<ChatMessageResponse> allMessages = new ArrayList<>();
        allMessages.addAll(chatMessages);
        allMessages.addAll(whisperMessages);

        // 시간순으로 정렬 (createdAt 기준)
        allMessages.sort(Comparator.comparing(ChatMessageResponse::getCreatedAt));

        // 페이지 크기와 페이지 번호 계산
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        int fromIndex = pageNumber * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, allMessages.size());

        // 페이지에 해당하는 메시지 서브 리스트 추출
        List<ChatMessageResponse> paginatedMessages = allMessages.subList(fromIndex, toIndex);

        // 페이지와 총 개수
        int totalPages = (int) Math.ceil((double) allMessages.size() / pageSize);
        long totalElements = allMessages.size();

        // 결과를 반환
        return ChatMessageListResponse.builder()
                .chatMessageResponseList(paginatedMessages)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .build();
    }

    /**
     * 귓속말 로그 저장
     * @param roomID
     * @param member
     * @param chatMessageRequest
     */
    private void handleWhisperMessage(Long roomID, Member member, ChatMessageRequest chatMessageRequest) {
        Member recipient = findMember(chatMessageRequest.getRecipientID());
        // 닉네임 -> 플레이 멤버 이름으로 변경
        PlayMember playMember = findPlayMember(chatMessageRequest.getRecipientID(),chatMessageRequest.getRoomID());
        CharacterSheet characterSheet = findCharacterSheet(playMember.getId());

        chatMessageRequest.setRecipientNickName(characterSheet.getName());

        PlayWhisperLog playWhisperLog = PlayWhisperLog.builder()
                .roomID(roomID)
                .memberID(member.getId())
                .recipientID(recipient.getId())
                .content(chatMessageRequest.getContent())
                .createdAt(LocalDateTime.now())
                .build();

        playWhisperLogRepository.save(playWhisperLog);
    }

    private Member findMember(Long memberID){
        return memberRepository.findById(memberID)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다. id=" + memberID));
    }

    private Boolean findRoom(Long roomID){
        boolean existRoom = roomRepository.existsById(roomID);
        if(!existRoom) throw new IllegalArgumentException("해당 방이 존재하지 않습니다. id=" + roomID);
        return true;
    }

    private PlayMember findPlayMember(Long memberID, Long roomID){
        return  playMemberRepository.findByMemberIdAndRoomId(memberID,roomID)
                .orElseThrow(()-> new IllegalArgumentException("해당 플레이 멤버가 존재하지 않습니다 id="+memberID+", "+roomID));
    }

    private CharacterSheet findCharacterSheet(Long playMemberID){
        return  characterSheetRepository.findByPlayMemberId(playMemberID)
                .orElseThrow(()-> new IllegalArgumentException("해당 캐릭터 시트가 존재하지 않습니다 id="+playMemberID));
    }

}
