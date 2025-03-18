package org.infinity.sixtalebackend.domain.character_sheet.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.infinity.sixtalebackend.domain.character_sheet.domain.CharacterEquipment;
import org.infinity.sixtalebackend.domain.character_sheet.domain.CharacterSheet;
import org.infinity.sixtalebackend.domain.character_sheet.dto.CharacterEquipmentRequest;
import org.infinity.sixtalebackend.domain.character_sheet.dto.CharacterEquipmentResponse;
import org.infinity.sixtalebackend.domain.character_sheet.dto.CharacterSheetEquipmentResponse;
import org.infinity.sixtalebackend.domain.character_sheet.dto.CharacterUpdateEquipmentRequest;
import org.infinity.sixtalebackend.domain.character_sheet.repository.CharacterEquipmentRepository;
import org.infinity.sixtalebackend.domain.character_sheet.repository.CharacterSheetRepository;
import org.infinity.sixtalebackend.domain.equipment.domain.Equipment;
import org.infinity.sixtalebackend.domain.room.domain.PlayMember;
import org.infinity.sixtalebackend.domain.room.repository.PlayMemberRepository;
import org.infinity.sixtalebackend.domain.scenario.domain.ScenarioEquipment;
import org.infinity.sixtalebackend.domain.scenario.repository.ScenarioEquipmentRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class CharacterSheetEquipmentServiceImpl implements CharacterSheetEquipmentService{
    private final CharacterSheetRepository characterSheetRepository;
    private final ScenarioEquipmentRepository scenarioEquipmentRepository;
    private final CharacterEquipmentRepository characterEquipmentRepository;
    private final PlayMemberRepository playMemberRepository;

    /**
     * 장비 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public CharacterSheetEquipmentResponse getCharacterSheetEquipment(Long roomID, Long playMemberID) {
        CharacterSheet characterSheet = characterSheetRepository.findByPlayMemberId(playMemberID)
                .orElseThrow(() -> new IllegalArgumentException(("Character Sheet not found")));

        // 캐릭터 장비 조회
        List<ScenarioEquipment> characterEquipmentList = scenarioEquipmentRepository.findByJobId(characterSheet.getJob().getId());

        // 공통 장비 목록을 조회
        List<ScenarioEquipment> commonEquipmentList = scenarioEquipmentRepository.findCommonEquipments();

        // CharacterSheet에 관련된 장비 정보 리스트를 작성
        List<CharacterSheetEquipmentResponse.EquipmentInfo> characterEquipmentInfoList = characterEquipmentList.stream()
                .map(this::mapToEquipmentInfo)
                .collect(Collectors.toList());

        // 공통 장비 정보 리스트를 작성
        List<CharacterSheetEquipmentResponse.EquipmentInfo> commonEquipmentInfoList = commonEquipmentList.stream()
                .map(this::mapToEquipmentInfo)
                .collect(Collectors.toList());

        return CharacterSheetEquipmentResponse.builder()
                .sheetID(characterSheet.getId())
                .characterEquipment(characterEquipmentInfoList)
                .commonEquipment(commonEquipmentInfoList)
                .build();
    }

    // ScenarioEquipment를 EquipmentInfo로 변환하는 메서드
    private CharacterSheetEquipmentResponse.EquipmentInfo mapToEquipmentInfo(ScenarioEquipment equipment) {
        return CharacterSheetEquipmentResponse.EquipmentInfo.builder()
                .id(equipment.getId())
                .name(equipment.getName())
                .description(equipment.getDescription())
                .typeID(equipment.getEquipmentType().getId())
                .typeName(equipment.getEquipmentType().getName())
                .weight(equipment.getWeight())
                .currentCount(equipment.getCount()) // 현재 수량을 그대로 사용합니다.
                .imageURL(equipment.getImageURL())
                .build();
    }
    /**
     *캐릭터 장비 추가
     */
    @Override
    @Transactional
    public Map<String, String> addCharacterEquipment(Long roomID, Long playMemberID, CharacterEquipmentRequest equipmentRequest) {
        PlayMember playMember = playMemberRepository.findByIdAndRoomId(playMemberID, roomID)
                .orElseThrow(() -> new IllegalArgumentException("Invalid PlayMember or Room ID"));
        CharacterSheet characterSheet = characterSheetRepository.findByPlayMemberId(playMemberID)
                .orElseThrow(() -> new IllegalArgumentException("Character Sheet not found"));

        // 장비 정보를 조회
        ScenarioEquipment equipment = scenarioEquipmentRepository.findById(equipmentRequest.getEquipmentId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Equipment ID: " + equipmentRequest.getEquipmentId()));
        // 추가할 무게
        int equipmentTotalWeight = equipmentRequest.getWeight();

        // 동일한 장비가 있는지 확인하고 수량 업데이트
        CharacterEquipment existingEquipment = characterEquipmentRepository.findByCharacterSheetAndEquipment(characterSheet, equipment);
        if (existingEquipment != null) {
            // 이미 존재하는 장비의 수량 업데이트(무게는 안바꾸죠?)
            existingEquipment.setCurrentCount(existingEquipment.getCurrentCount() + equipmentRequest.getCurrentCount());
//            existingEquipment.setWeight(existingEquipment.getWeight() + equipmentTotalWeight);
            characterEquipmentRepository.save(existingEquipment);
        } else {
            // 새 장비를 추가
            CharacterEquipment newEquipment = CharacterEquipment.builder()
                    .playMember(playMember)
                    .characterSheet(characterSheet)
                    .equipment(equipment)
                    .currentCount(equipmentRequest.getCurrentCount())
                    .weight(equipmentTotalWeight)
                    .build();
            characterEquipmentRepository.save(newEquipment);

            // CharacterSheet의 현재 무게 업데이트
            characterSheet.setCurrentWeight(characterSheet.getCurrentWeight() + equipmentTotalWeight);
            characterSheetRepository.save(characterSheet);
        }
        // 응답을 위한 map 생성
        Map<String, String> result = new HashMap<>();
        result.put("sheetID", characterSheet.getId().toString());
        result.put("characterName", characterSheet.getName());

        return result;
    }

    /**
     *캐릭터 장비 수량 업데이트
     */
    @Override
    @Transactional
    public void updateCharacterEquipment(Long roomID, Long playMemberID, CharacterUpdateEquipmentRequest characterUpdateEquipmentRequest) {
        PlayMember playMember = playMemberRepository.findByIdAndRoomId(playMemberID, roomID)
                .orElseThrow(() -> new IllegalArgumentException("Invalid PlayMember or Room ID"));
        CharacterSheet characterSheet = characterSheetRepository.findByPlayMemberId(playMemberID)
                .orElseThrow(() -> new IllegalArgumentException("Character Sheet not found"));

        // 장비 정보를 조회
        ScenarioEquipment equipment = scenarioEquipmentRepository.findById(characterUpdateEquipmentRequest.getEquipmentId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Equipment ID: " + characterUpdateEquipmentRequest.getEquipmentId()));

        // 동일한 장비가 있는지 확인하고 수량 업데이트
        CharacterEquipment existingEquipment = characterEquipmentRepository.findByCharacterSheetAndEquipment(characterSheet, equipment);
        if (existingEquipment != null) {
            existingEquipment.setCurrentCount(characterUpdateEquipmentRequest.getCurrentCount());
            characterEquipmentRepository.save(existingEquipment);
        } else {
            throw new IllegalArgumentException("Character Equipment not found");
        }
    }


    /**
     * 캐릭터 장비 삭제
     * equipment_id 가 삭제
     */
    @Override
    @Transactional
    public Map<String, String> deleteCharacterEquipment(Long roomID, Long playMemberID, Long equipmentID) {
        CharacterSheet characterSheet = characterSheetRepository.findByPlayMemberId(playMemberID)
                .orElseThrow(() -> new IllegalArgumentException("Character Sheet not found"));

        CharacterEquipment equipment = characterEquipmentRepository.findByCharacterSheetAndEquipment_Id(characterSheet, equipmentID)
                .orElseThrow(() -> new IllegalArgumentException("Equipment not found"));

        characterEquipmentRepository.delete(equipment);
        int currentWeight =characterSheet.getCurrentWeight();

        // 삭제된 장비의 무게를 현재 무게에서 뺌
        int updatedWeight = characterSheet.getCurrentWeight() - equipment.getWeight();
        characterSheet.setCurrentWeight(updatedWeight);

        // 변경된 무게 저장
        characterSheetRepository.save(characterSheet);
        log.info("장비 삭제 완료 - 장비 ID: {}, 업데이트된 무게: {}", equipmentID, updatedWeight);

        // 응답을 위한 map 생성
        Map<String, String> result = new HashMap<>();
        result.put("roomID", roomID.toString());
        result.put("sheetID", characterSheet.getId().toString());
        result.put("currentWeight", String.valueOf(currentWeight));
        result.put("updateWeight", String.valueOf(updatedWeight));

        return result;
    }
}
