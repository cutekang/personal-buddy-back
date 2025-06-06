package com.app.personalbuddyback.service;

import com.app.personalbuddyback.domain.ChatRoomVO;
import com.app.personalbuddyback.domain.ChatRoomViewDTO;
import com.app.personalbuddyback.domain.ChatVO;
import com.app.personalbuddyback.domain.ChatViewDTO;
import com.app.personalbuddyback.repository.ChattingDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class ChattingServiceImpl implements ChattingService {

    private final ChattingDAO chattingDAO;

    @Override
    public void saveChatAndUpdateChatRoomLastMessage(ChatVO chatVO) {
        // 채팅 기록을 저장
        chattingDAO.saveChat(chatVO);
        // 채팅방의 마지막 메세지와 시간을 기록
        chattingDAO.updateChatRoom(chatVO);
    }

    @Override
    public List<ChatViewDTO> updateChatReadAndGetAllChat(Long memberId, Long chatRoomId) {
        List<ChatViewDTO> chats = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        
        map.put("memberId", memberId);
        map.put("chatRoomId", chatRoomId);
        
        // 상대가 보낸 채팅 기록을 읽음 처리
        chattingDAO.updateChatRead(map);
        // 채팅방의 채팅기록
        chats = chattingDAO.findChat(chatRoomId);
        return chats;
    }

    @Override
    public List<ChatRoomViewDTO> findAllChatRoomByMemberIdAndFilter(Long memberId, String filterType, String searchNickname) {
        List<ChatRoomViewDTO> chatRooms = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("memberId", memberId);
        map.put("searchNickname", searchNickname);
        map.put("filterType", filterType);

        chatRooms = chattingDAO.findAllChatRoom(map);

        return chatRooms;
    }

    @Override
    public Optional<ChatRoomViewDTO> saveChatRoomOrChangeViewChatRoom(Long memberId, Long secondMemberId) {
        Map<String, Object> membersMap = new HashMap<>();
        Map<String, Object> findMemberPositionInfo = new HashMap<>();
        Map<String, Object> changeViewInfo = new HashMap<>();

        membersMap.put("firstMemberId", memberId);
        membersMap.put("secondMemberId", secondMemberId);

        // 이미 존재하는 채팅방이면 view로 변환후 정보 반환
        Optional<Long> chatRoomId = chattingDAO.findChatRoomIsTrue(membersMap);

        if (chatRoomId.isPresent()) {
            Long roomId = chatRoomId.get();
            findMemberPositionInfo.put("chatRoomId", roomId);
            findMemberPositionInfo.put("memberId", memberId);

            Optional<String> memberPosition = chattingDAO.findChatMemberPosition(findMemberPositionInfo);
            changeViewInfo.put("position", memberPosition.get());
            changeViewInfo.put("chatRoomId", roomId);

            chattingDAO.updateViewChatRoom(changeViewInfo);

            return chattingDAO.findChatRoom(findMemberPositionInfo);
        } else {
            // 새로운 채팅방 생성 후 반환
            ChatRoomVO chatRoomVO = new ChatRoomVO();
            chatRoomVO.setFirstMemberId(memberId);
            chatRoomVO.setSecondMemberId(secondMemberId);

            chattingDAO.saveChatRoom(chatRoomVO);

            findMemberPositionInfo.put("memberId", memberId);
            findMemberPositionInfo.put("chatRoomId", chatRoomVO.getId());
            return chattingDAO.findChatRoom(findMemberPositionInfo);
        }
    }

    @Override
    public Optional<Integer> findAllNotReadChat(Long memberId) {
        return chattingDAO.findAllNotReadChat(memberId);
    }

    @Override
    public void updateChangeHideByChatRoomIdAndMemberId(Long chatRoomId, Long memberId) {
        Map<String, Object> findMemberPositionInfo = new HashMap<>();
        Map<String, Object> hideChattinginfo = new HashMap<>();

        findMemberPositionInfo.put("chatRoomId", chatRoomId);
        findMemberPositionInfo.put("memberId", memberId);
        
        // 멤버의 포지션
        Optional<String> memberPosition = chattingDAO.findChatMemberPosition(findMemberPositionInfo);
        hideChattinginfo.put("position", memberPosition.get());
        hideChattinginfo.put("chatRoomId", chatRoomId);

        // 해당 채팅방의 멤버 포지션에 해당하는 view값을 숨긴다.
        chattingDAO.updateHideChatRoom(hideChattinginfo);
    }

    @Override
    public void updateHideChatByChatIdAndMemberId(Long chatId, Long memberId) {
        Map<String, Object> changeHideChatInfo = new HashMap<>();
        changeHideChatInfo.put("chatId", chatId);
        changeHideChatInfo.put("memberId", memberId);
        chattingDAO.updateHideChat(changeHideChatInfo);
    }
}
