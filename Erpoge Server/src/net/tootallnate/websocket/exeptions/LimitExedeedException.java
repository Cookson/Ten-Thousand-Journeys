package net.tootallnate.websocket.exeptions;

import net.tootallnate.websocket.CloseFrame;

public class LimitExedeedException extends InvalidDataException {
	public static final long serialVersionUID = 3943338846L; 


	public LimitExedeedException() {
		super( CloseFrame.TOOBIG );
	}

	public LimitExedeedException( String s ) {
		super( CloseFrame.TOOBIG, s );
	}

}
