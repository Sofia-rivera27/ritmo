package co.edu.uptc.model;

import java.util.ArrayList;
import java.util.List;

public class RitmoSystem implements IModel{
	private List<UserListener> users;
	private Admin admin;	
	
	public RitmoSystem() {
		users = new ArrayList<UserListener>();
		admin = new Admin();
	}

	public List<UserListener> getUsers() {
		return users;
	}

	public void setUsers(List<UserListener> users) {
		this.users = users;
	}

	public Admin getAdmin() {
		return admin;
	}

	public void setAdmin(Admin admin) {
		this.admin = admin;
	}	
}
