package ttt_lobby_service.domain;

import common.ddd.ValueObject;

public record UserId(String id) implements ValueObject {

	public boolean equals(Object obj) {
		return id().equals(((UserId)obj).id());
	}
	
	@Override
	public int hashCode() {
		return id().hashCode();
	}
}
