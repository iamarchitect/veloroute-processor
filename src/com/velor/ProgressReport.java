package com.velor;

import java.util.Date;

public class ProgressReport {
	long start = -1;
	double total = 0;
	int pcent = 0;
	int current = 0;
	int pcent_ = 0;

	public ProgressReport() {
		super();
		start = new Date().getTime();
	}

	void update() {
		current++;
		if (total > 0) {
			display();
		} else {
			displayIndeterminate();
		}
	}

	protected void display() {
		pcent = (int) (current / total * 100);
		if (pcent != pcent_) {
			if (pcent % 10 == 0) {
				System.out.print(pcent + "%");
			} else {
				System.out.print(".");
			}
		}
		pcent_ = pcent;
		if (pcent >= 100) {
			System.out.println();
			System.out.println("finished in " + (new Date().getTime() - start)
					+ " milliseconds");
		}
	}

	protected void displayIndeterminate() {
		if (start == -1) {
			start = new Date().getTime();
		}

		if (new Date().getTime() - start > 1000) {
			start = new Date().getTime();
			System.out.print(".");
		}
	}

	void update(int n) {
		current += n;
		if (total > 0) {
			display();
		} else {
			displayIndeterminate();
		}
	}
}