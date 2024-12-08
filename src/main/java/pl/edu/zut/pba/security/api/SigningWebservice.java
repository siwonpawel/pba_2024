package pl.edu.zut.pba.security.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import pl.edu.zut.pba.security.JwtsService;
import pl.edu.zut.pba.security.api.model.ContentSigningResponse;

@RestController
@AllArgsConstructor
public class SigningWebservice
{

    private final JwtsService jwtsService;

    @PostMapping("/api/signing")
    public ResponseEntity<ContentSigningResponse> generatePayload(@RequestBody String payload)
    {
        return ResponseEntity.ok(new ContentSigningResponse(jwtsService.generateSignature(payload)));
    }

}
