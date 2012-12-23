package erpoge.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

public class AspectRangedWeapon extends Aspect implements GsonForStaticDataSerializable {
	private int reloadTime;
	private int aimTime;
	private int magazine;
	private String ammoType;
	public AspectRangedWeapon(int reloadTime, int aimTime, int magazine, String ammoType) {
		super(AspectName.RANGED_WEAPON);
		this.reloadTime = reloadTime;
		this.aimTime = aimTime;
		this.magazine = magazine;
		this.ammoType = ammoType;
	}
	public String toString() {
		return "As ranged weapon:\n"
			+"Reload time — "+reloadTime+"\n"
			+"aim time — "+aimTime+"\n"
			+"magazine — "+magazine+"\n"
			+"ammoType — "+ammoType;
	}
	@Override
	public JsonElement serialize(JsonSerializationContext context) {
		JsonArray jArray = new JsonArray();
		jArray.add(new JsonPrimitive(reloadTime));
		jArray.add(new JsonPrimitive(aimTime));
		jArray.add(new JsonPrimitive(magazine));
		jArray.add(new JsonPrimitive(ammoType));
		return jArray;
	}
}
