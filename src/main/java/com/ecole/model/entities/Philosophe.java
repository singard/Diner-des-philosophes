package com.ecole.model.entities;

import java.util.Random;
import java.util.concurrent.Semaphore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@RequiredArgsConstructor
@AllArgsConstructor
public class Philosophe implements Runnable {

	private int numero;
	private Etat etat;
	private Semaphore[] baguettes;
	private int nbPhilosophes;
	
	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				if (etat==Etat.MANGER || etat==Etat.PENSER) {//si le philosophe change d'�tat principale alors une dir� al�atoire lui est attribuer
					Random random = new Random();
					int randomNumber = random.nextInt(1+numero%3) + 1;//nombre al�atoire entre 1 et 3
					Thread.sleep(randomNumber * 1000);//durer compris entre 1 et 3 secondes
				}
				log.info("num�ro "+numero+" fait des choses");


				switch (etat) {
				case PENSER:

//					System.out.println("num�ro "+numero+" je veux manger mais : "+voisinGauche.getEtat()+" et "+voisinDroite.getEtat());
					synchronized (baguettes) {
						if (fourchettesDisponibles()) {

							// Acquisition des baguettes
							baguettes[numero ].acquire(); // Baguette droite
							baguettes[(numero +1) % nbPhilosophes].acquire(); // Baguette gauche
							synchronized (etat) {
								etat = Etat.MANGER;
								changerEtat();
							}

						}else {
							synchronized (etat) {
								etat = Etat.BESOIN_DE_MANGER;
								changerEtat();
							}

						}

					}
					break;

				case MANGER:


					// Lib�ration des baguettes
					baguettes[numero].release(); // Baguette droite
					baguettes[(numero +1) % nbPhilosophes].release(); // Baguette gauche
					synchronized (etat) {

						etat = Etat.PENSER;


					}

					changerEtat();
					break;

				case BESOIN_DE_MANGER:
					synchronized (baguettes) { 
						if (fourchettesDisponibles()) {
							// Acquisition des baguettes
							baguettes[numero].acquire(); // Baguette droite
							baguettes[(numero +1) % nbPhilosophes].acquire(); // Baguette gauche
						synchronized (etat) {
							etat = Etat.MANGER;
							changerEtat();
						}
					}

					}
					


					break;
				}	


			}catch(Exception e) {
				e.printStackTrace();
			}
		}




	}


	public void changerEtat() throws InterruptedException {

//		System.out.println("je suis num�ro "+numero+" j'ai changer d'�tat "+etat);
	}
	public boolean fourchettesDisponibles() {
	    return baguettes[numero ].availablePermits() > 0 && baguettes[(numero +1) % nbPhilosophes].availablePermits() > 0;
	}






}
