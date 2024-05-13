package de.zuellich.meal_planner.pinterest.services;

import de.zuellich.meal_planner.pinterest.datatypes.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

/**
 * A service to call the Pinterest Board API.
 */
@Service
public class BoardService implements IBoardService {

    public static final String USERS_BOARDS = "https://api.pinterest.com/v1/me/boards";

    public static final String BOARDS_PINS = "https://api.pinterest.com/v1/boards/{id}/pins/?fields=id,original_link,note,metadata";

    public static final String BOARDS_PINS_WITH_CURSOR = BOARDS_PINS + "&cursor={cursor}";

    private static final String GET_BOARD = "https://api.pinterest.com/v1/boards/{id}/?fields=id,name,url";

    private final OAuth2RestOperations restTemplate;

    /**
     * @param restTemplate A rest template that manages our OAuth2 access tokens etc.
     */
    @Autowired
    public BoardService(final OAuth2RestOperations restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    @Cacheable("boards")
    public List<Board> getBoards() {
        try {
            final ResponseEntity<BoardList> boards = restTemplate.getForEntity(USERS_BOARDS, BoardList.class);
            return boards.getBody().getBoards();
        } catch (final RestClientException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    @Cacheable("pins")
    public List<Pin> getPins(final String boardId) {
        final List<Pin> result = new ArrayList<>();
        String cursor = "";
        do {
            String requestURL = BOARDS_PINS;
            final Map<String, String> requestParameter = new HashMap<>(3);
            requestParameter.put("id", boardId);
            if (!cursor.isEmpty()) {
                requestParameter.put("cursor", cursor);
                requestURL = BOARDS_PINS_WITH_CURSOR;
            }
            final ResponseEntity<PinList> response = restTemplate.getForEntity(requestURL, PinList.class, requestParameter);
            result.addAll(response.getBody().getPins());
            if (!response.getBody().getPage().getCursor().isEmpty()) {
                cursor = response.getBody().getPage().getCursor();
            } else {
                cursor = "";
            }
        } while (!cursor.isEmpty());
        return result;
    }

    @Override
    @Cacheable("boardListing")
    public BoardListing getBoardListing(final String boardId) {
        final ResponseEntity<BoardRequest> board = restTemplate.getForEntity(GET_BOARD, BoardRequest.class, boardId);
        final List<Pin> pins = getPins(boardId);
        final BoardListing result = new BoardListing();
        result.setBoard(board.getBody().getBoard());
        result.setPins(pins);
        return result;
    }
}
