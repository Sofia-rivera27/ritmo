package co.edu.uptc.presenter;

import co.edu.uptc.model.IModel;
import co.edu.uptc.model.Model;
import co.edu.uptc.view.IFrame;
import co.edu.uptc.view.ViewFrame;

public class Presenter {
	private IFrame frame;
	private IModel io;
	
	public Presenter() {
		frame = new ViewFrame();
		io = new Model();
		addListeners();
	}

	private void addListeners() {
		
	}
}
