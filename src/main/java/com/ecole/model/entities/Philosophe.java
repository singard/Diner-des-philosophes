package com.ecole.model.entities;

import java.util.Random;
import java.util.concurrent.Semaphore;

import com.ecole.ApplicationMain;

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

	@Override
	public void run() {
		 //calcule � chaque passage, le temps qui s'est �coul� pour s'arr�ter � la dur�e param�tr�e
		long startTime = System.currentTimeMillis();
		long elapsedTime = 0;
		while (elapsedTime < ApplicationMain.EXECUTION_TIME_IN_SEC * 1000) {
			try {
				/* Si le philosophe change d'�tat principale (manger ou penser) alors une dir� al�atoire lui est attribuer 
				 * qui sera la durer qu'il va attendre avant de changer d'�tat.
				 * C'est nombre al�atoire entre 1 et 3 seconde
				 */
				if (etat==Etat.MANGER || etat==Etat.PENSER) {
					Random random = new Random();
					int randomNumber = random.nextInt(1+numero%3) + 1;
					Thread.sleep(randomNumber * 1000);
				}
				log.trace("num�ro "+numero+" fait des choses");

				switch (etat) {
				case PENSER:

					synchronized (baguettes) {
						if (fourchettesDisponibles()) {
							// Acquisition des baguettes
							baguettes[numero ].acquire(); // Baguette de droite
							baguettes[(numero +1) % ApplicationMain.NB_PHILOSOPHE].acquire(); // Baguette de gauche

							etat = Etat.MANGER;
							changerEtat();

						}else {
							etat = Etat.BESOIN_DE_MANGER;
							changerEtat();
						}
					}
					break;

				case MANGER:
					// Lib�ration des baguettes
					baguettes[numero].release(); // Baguette de droite
					baguettes[(numero +1) % ApplicationMain.NB_PHILOSOPHE].release(); // Baguette de gauche

					etat = Etat.PENSER;
					changerEtat();
					break;

				case BESOIN_DE_MANGER:
					synchronized (baguettes) { 
						if (fourchettesDisponibles()) {
							// Acquisition des baguettes
							baguettes[numero].acquire(); // Baguette droite
							baguettes[(numero +1) % ApplicationMain.NB_PHILOSOPHE].acquire(); // Baguette gauche

							etat = Etat.MANGER;
							changerEtat();							
						}
					}
					break;
				}	
			}catch(Exception e) {
				e.printStackTrace();
			}
			//calcule � chaque passage, le temps qui s'est �coul� pour s'arr�ter � la dur�e param�tr�e
			elapsedTime = System.currentTimeMillis() - startTime;
		}
	}

	private  void changerEtat() throws InterruptedException {
		log.debug("je suis num�ro "+numero+" j'ai changer d'�tat "+etat);
	}

	private boolean fourchettesDisponibles() {
		// Cette action est dans un synchronized cela veut dire que les philosophes peuvent y avoir acc�s une personne � la fois
		return baguettes[numero ].availablePermits() > 0 && baguettes[(numero +1) % ApplicationMain.NB_PHILOSOPHE].availablePermits() > 0;
	}






}
