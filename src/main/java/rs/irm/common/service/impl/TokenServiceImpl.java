package rs.irm.common.service.impl;

import java.util.Date;

import io.jsonwebtoken.Jwts;
import jakarta.inject.Named;
import rs.irm.common.enums.Language;
import rs.irm.common.exceptions.WrongTokenException;
import rs.irm.common.service.TokenService;
import rs.irm.utils.AppKeys;
import rs.irm.utils.AppParameters;

@Named
public class TokenServiceImpl implements TokenService {

	@Override
	public String generateToken(String username, Language language) {
		Date startDate = new Date();
		Date endDate = new Date(startDate.getTime() + AppParameters.sessionduration);

		return Jwts.builder().subject(username).issuedAt(startDate).expiration(endDate).header()
				.add("language", language.name()).and().signWith(AppKeys.privateKey).compact();
	}

	@Override
	public void validToken(String token) {
		try {
			Jwts.parser().verifyWith(AppKeys.publicKey).build().parse(token);
		} catch (Exception e) {
			throw new WrongTokenException(token);
		}

	}

	@Override
	public String getUsername(String token) {
		return Jwts.parser().verifyWith(AppKeys.publicKey).build().parseSignedClaims(token).getPayload().getSubject();
	}
	
	@Override
	public String getLanguage(String token) {
		return Jwts.parser().verifyWith(AppKeys.publicKey).build().parseSignedClaims(token)
				.getHeader().get("language").toString();
	}

}
