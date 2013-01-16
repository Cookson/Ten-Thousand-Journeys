package erpoge.core.net.serverevents;

public class EventMeleeAttack extends ServerEvent {
	public static final String e = "meleeAttack";
	public int attackerId;
	public int aimId;
	public EventMeleeAttack(int attackerId, int aimId) {
		this.attackerId = attackerId;
		this.aimId = aimId;
	}
}
