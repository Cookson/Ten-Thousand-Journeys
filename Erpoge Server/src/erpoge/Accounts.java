package erpoge;

import java.util.HashMap;

public class Accounts {
	public final static HashMap<String, Account> accounts = new HashMap<String, Account>();
	public static void addAccount(Account account) {
		accounts.put(account.login, account);
	}
	public static Account account(String login) {
		return accounts.get(login);
	}
	public static boolean hasAccount(String login) {
		return accounts.containsKey(login);
	}
}
