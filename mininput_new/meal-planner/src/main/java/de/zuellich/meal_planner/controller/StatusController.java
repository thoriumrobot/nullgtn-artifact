package de.zuellich.meal_planner.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 */
@RestController
public class StatusController {

    private OAuth2RestOperations restTemplate;

    @Autowired
    public StatusController(OAuth2RestOperations restTemplate) {
        this.restTemplate = restTemplate;
    }

    @RequestMapping("/status")
    public ResponseEntity<Boolean> getStatus() {
        /*
     * Access through the context, otherwise Spring OAuth2 will attempt to fetch a token which will fail due to the
     * redirect configuration (this route is not included in the redirect URls) and we do not want a request to be send.
     */
        OAuth2AccessToken accessToken = restTemplate.getOAuth2ClientContext().getAccessToken();
        boolean accessTokenAvailable = accessToken != null && !accessToken.isExpired();
        return ResponseEntity.ok(accessTokenAvailable);
    }
}
