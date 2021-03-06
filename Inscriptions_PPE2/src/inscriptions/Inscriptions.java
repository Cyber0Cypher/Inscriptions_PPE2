package inscriptions;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.time.LocalDate;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.sound.sampled.BooleanControl;

import org.junit.experimental.theories.internal.BooleanSupplier;

import DB.Requete;
import dialogue.Action;
import dialogue.Menu;
import dialogue.Option;

/**
 * Point d'entrÃ©e dans l'application, un seul objet de type Inscription
 * permet de gÃ©rer les compÃ©titions, candidats (de type equipe ou personne)
 * ainsi que d'inscrire des candidats Ã  des compÃ©tition.
 */

public class Inscriptions implements Serializable
{
	private static final long serialVersionUID = -3095339436048473524L;
	private static final String FILE_NAME = "Inscriptions.srz";
	private static Inscriptions inscriptions;
	
	// Objet permettant l'accès aux procédure stockées de la BD
	private Requete r = new Requete();
	
	private SortedSet<Competition> competitions = new TreeSet<>();
	private SortedSet<Candidat> candidats = new TreeSet<>();

	private Inscriptions()
	{
		ArrayList<ArrayList<String>> lesCompets = r.getCompetition();
		ArrayList<ArrayList<String>> lesEquipes = r.getEquipe();
		ArrayList<ArrayList<String>> lesPersonnes = r.getPersonne();
		
		// iniatialisation des compÃ©titions
		for (int j = 0; j < lesCompets.get(0).size(); j++) {
			createCompetition(Integer.parseInt(lesCompets.get(0).get(j)), lesCompets.get(1).get(j), LocalDate.parse(lesCompets.get(2).get(j)), convertToBoolean(lesCompets.get(3).get(j)));
		}
		
		// initialisation des personnes
		for (int j = 0; j < lesPersonnes.get(0).size(); j++) {
			createPersonne(Integer.parseInt(lesPersonnes.get(0).get(j)), lesPersonnes.get(1).get(j), lesPersonnes.get(2).get(j), lesPersonnes.get(3).get(j));
		}
		
		// initialisation des equipes
		for (int j = 0; j < lesEquipes.get(0).size(); j++) {
			createEquipe(Integer.parseInt(lesEquipes.get(0).get(j)), lesEquipes.get(1).get(j));
		}
		
		// initialisation des membres de chaque Ã©quipe
		for(Equipe e : getEquipes()) {
			ArrayList<ArrayList<String>> lesMembres = r.getPersonneEquipe(e.getId());
			for (int j = 0; j < r.getPersonneEquipe(e.getId()).get(0).size(); j++) {
				for(Personne p : getPersonnes()) {
					if(p.getId() == Integer.parseInt(lesMembres.get(0).get(j))) {
						e.add(p);
					}
				}
			}
		}
		
		// initialisation des participants de chaque compÃ©tition
		for(Competition c : getCompetitions()) {
			ArrayList<ArrayList<String>> lesCandidats = r.candidatsInscritsCompetition(c.getId());
			for (int j = 0; j < r.candidatsInscritsCompetition(c.getId()).get(0).size(); j++) {
				if(!c.estEnEquipe()) {
					for(Personne p : getPersonnes()) {
						if(p.getId() == Integer.parseInt(lesCandidats.get(0).get(j))) {
							c.add(p);
						}
					}
				}
				if(c.estEnEquipe()) {
					for(Equipe e : getEquipes()) {
						if(e.getId() == Integer.parseInt(lesCandidats.get(0).get(j))) {
							c.add(e);
						}
					}
				}
			}
		}
		
	}
	
	/**
	 * Retourne les compÃ©titions.
	 * @return
	 */
	
	public SortedSet<Competition> getCompetitions()
	{
		return Collections.unmodifiableSortedSet(competitions);
		
	}
	
	public int getAICompetitions()
	{
		return Integer.parseInt(r.getAICompetition().get(10).get(0));
	}
	
	/**
	 * Retourne tous les candidats (personnes et Ã©quipes confondues).
	 * @return
	 */
	
	public SortedSet<Candidat> getCandidats()
	{
		return Collections.unmodifiableSortedSet(candidats);
	}
	
	public int getAICandidats()
	{
		return Integer.parseInt(r.getAICandidat().get(10).get(0));
	}

	/**
	 * Retourne toutes les personnes.
	 * @return
	 */
	
	public SortedSet<Personne> getPersonnes()
	{
		SortedSet<Personne> personnes = new TreeSet<>();
		for (Candidat c : getCandidats())
			if (c instanceof Personne)
				personnes.add((Personne)c);
		return Collections.unmodifiableSortedSet(personnes);
	}

	/**
	 * Retourne toutes les Ã©quipes.
	 * @return
	 */
	
	public SortedSet<Equipe> getEquipes()
	{
		SortedSet<Equipe> equipes = new TreeSet<>();
		for (Candidat c : getCandidats())
			if (c instanceof Equipe)
				equipes.add((Equipe)c);
		return Collections.unmodifiableSortedSet(equipes);
	}

	/**
	 * CrÃ©Ã©e une compÃ©tition. Ceci est le seul moyen, il n'y a pas
	 * de constructeur public dans {@link Competition}.
	 * @param id
	 * @param nom
	 * @param dateCloture
	 * @param enEquipe
	 * @return
	 */
	
	public Competition createCompetition(int id, String nom, LocalDate dateCloture, boolean enEquipe)
	{
		if(id == this.getAICompetitions())
			r.creerCompetition(nom, dateCloture.toString(), enEquipe);
		
		Competition competition = new Competition(this, id, nom, dateCloture, enEquipe);
		competitions.add(competition);
		return competition;
	}

	/**
	 * CrÃ©Ã©e une Candidat de type Personne. Ceci est le seul moyen, il n'y a pas
	 * de constructeur public dans {@link Personne}.

	 * @param id
	 * @param nom
	 * @param prenom
	 * @param mail
	 * @return
	 */
	
	public Personne createPersonne(int id, String nom, String prenom, String mail)
	{
		if(id == this.getAICandidats())
			r.creerPersonne(nom, prenom, mail);
		
		Personne personne = new Personne(this, id, nom, prenom, mail);
		candidats.add(personne);
		return personne;
	}
	
	/**
	 * CrÃ©Ã©e une Candidat de type Ã©quipe. Ceci est le seul moyen, il n'y a pas
	 * de constructeur public dans {@link Equipe}.
	 * @param id
	 * @param nom
	 * @param prenom
	 * @param mail
	 * @return
	 */
	
	public Equipe createEquipe(int id, String nom)
	{
		if(id == getAICandidats())
			r.creerEquipe(nom);
		
		Equipe equipe = new Equipe(this, id, nom);
		candidats.add(equipe);
		return equipe;
	}
	
	void remove(Competition competition)
	{
		competitions.remove(competition);
	}
	
	void remove(Candidat candidat)
	{
		candidats.remove(candidat);
	}
	
	/**
	 * Retourne l'unique instance de cette classe.
	 * CrÃ©e cet objet s'il n'existe dÃ©jÃ .
	 * @return l'unique objet de type {@link Inscriptions}.
	 */
	
	public static Inscriptions getInscriptions()
	{
		
		if (inscriptions == null)
		{
			inscriptions = new Inscriptions();
		}
		return inscriptions;
	}

	/**
	 * Retourne un object inscriptions vide. Ne modifie pas les compÃ©titions
	 * et candidats dÃ©jÃ  existants.
	 */
	
	public Inscriptions reinitialiser()
	{
		inscriptions = new Inscriptions();
		return getInscriptions();
	}

	/**
	 * Efface toutes les modifications sur Inscriptions depuis la derniÃ¨re sauvegarde.
	 * Ne modifie pas les compÃ©titions et candidats dÃ©jÃ  existants.
	 */
	
	public Inscriptions recharger()
	{
		inscriptions = null;
		return getInscriptions();
	}
	
	private static Inscriptions readObject()
	{
		ObjectInputStream ois = null;
		try
		{
			FileInputStream fis = new FileInputStream(FILE_NAME);
			ois = new ObjectInputStream(fis);
			return (Inscriptions)(ois.readObject());
		}
		catch (IOException | ClassNotFoundException e)
		{
			return null;
		}
		finally
		{
				try
				{
					if (ois != null)
						ois.close();
				} 
				catch (IOException e){}
		}	
	}
	
	/**
	 * Sauvegarde le gestionnaire pour qu'il soit ouvert automatiquement 
	 * lors d'une exÃ©cution ultÃ©rieure du programme.
	 * @throws IOException 
	 */
	
	public void sauvegarder() throws IOException
	{
		ObjectOutputStream oos = null;
		try
		{
			FileOutputStream fis = new FileOutputStream(FILE_NAME);
			oos = new ObjectOutputStream(fis);
			oos.writeObject(this);
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			try
			{
				if (oos != null)
					oos.close();
			} 
			catch (IOException e){}
		}
	}
	
	@Override
	public String toString()
	{
		return "Candidats : " + getCandidats().toString()
			+ "\nCompetitions  " + getCompetitions().toString();
	}
	
	private boolean convertToBoolean(String value) {
		
	    boolean returnValue = false;
	    if ("1".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || 
	        "true".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value))
	        returnValue = true;
	    return returnValue;
	}
	
	public static void main(String[] args)
	{
//		Inscriptions inscriptions = Inscriptions.getInscriptions();
//		Competition flechettes = inscriptions.createCompetition("Mondial de flÃ©chettes", null, false);
//		Personne tony = inscriptions.createPersonne("Tony", "Dent de plomb", "azerty"), 
//				boris = inscriptions.createPersonne("Boris", "le Hachoir", "ytreza");
//		flechettes.add(tony);
//		Equipe lesManouches = inscriptions.createEquipe("Les Manouches");
//		lesManouches.add(boris);
//		lesManouches.add(tony);
//		System.out.println(inscriptions);
//		lesManouches.delete();
//		System.out.println(inscriptions);
		
		Inscriptions inscriptions = Inscriptions.getInscriptions();
		
		Requete r = new Requete();
		
		System.out.println("\n ----------------------------------- \n Utilitaire des inscriptions sportives M2L \n ----------------------------------- \n");
		
		// Menu principal
		Menu menuPrincipal = new Menu("Menu Princpial");
		
		
		/*
		 * 
		 */
		
		// Menu compÃ©tition et ajout de ses options
		Menu menuCompetition = new Menu("CompÃ©titions", "a");
		menuCompetition.ajouteRevenir("r");
		
		// inscrit un candidat Ã  la compÃ©tition
		menuCompetition.ajoute(new Option("CrÃ©er une compÃ©tition", "a", new Action() {
			public void optionSelectionnee() {
				String nom = utilitaires.EntreesSorties.getString("Saisissez le nom de la nouvelle compÃ©tition: ");
				String date = utilitaires.EntreesSorties.getString("Saisissez la date de cloture: ");
				int enEquipe = utilitaires.EntreesSorties.getInt("CompÃ©tition en Ã©quipe ? oui 1, non 0: ");
				//r.creerCompetition(nom, date, enEquipe);
			}
		}));
		menuCompetition.ajoute(new Option("Supprimer une compÃ©tition", "b", new Action() {
			public void optionSelectionnee() {
				r.getCompetition();
				int idComp = utilitaires.EntreesSorties.getInt("Saisissez l'id de la compÃ©tition: ");
				r.supprimerCompetition(idComp);
			}
		}));
		menuCompetition.ajoute(new Option("Candidats inscrits Ã  une compÃ©tition", "c", new Action() {
			public void optionSelectionnee() {
				r.getCompetition();
				int idComp = utilitaires.EntreesSorties.getInt("Saisissez l'id de la compÃ©tition: ");
				r.candidatsInscritsCompetition(idComp);
			}
		}));
		menuCompetition.ajoute(new Option("Nom d'une compÃ©tition", "d", new Action() {
			public void optionSelectionnee() {
				r.getCompetition();
				int idComp = utilitaires.EntreesSorties.getInt("Saisissez l'id de la compÃ©tition: ");
				r.nomCompetition(idComp);
			}
		}));
		menuCompetition.ajoute(new Option("date de cloture des compÃ©titions", "e", new Action() {
			public void optionSelectionnee() {
				r.getCompetition();
				int idComp = utilitaires.EntreesSorties.getInt("Saisissez l'id de la compÃ©tition: ");
				r.dateClotureInscription(idComp);
			}
		}));
		menuCompetition.ajoute(new Option("Les inscriptions d'une compÃ©tition sont elles encore ouvertes", "f", new Action() {
			public void optionSelectionnee() {
				r.getCompetition();
				int idComp = utilitaires.EntreesSorties.getInt("Saisissez l'id de la compÃ©tition: ");
				r.inscriptionsOuvertes(idComp);
			}
		}));
		menuCompetition.ajoute(new Option("Modifier le nom d'une compÃ©tition", "g", new Action() {
			public void optionSelectionnee() {
				r.getCompetition();
				int idComp = utilitaires.EntreesSorties.getInt("Saisissez l'id de la compÃ©tition: ");
				String nom = utilitaires.EntreesSorties.getString("Saisissez le nouveau nom: ");
				r.modifierNomCompetition(idComp, nom);
			}
		}));
		menuCompetition.ajoute(new Option("Modifier date de cloture d'une compÃ©tition", "h", new Action() {
			public void optionSelectionnee() {
				r.getCompetition();
				int idComp = utilitaires.EntreesSorties.getInt("Saisissez l'id de la compÃ©tition: ");
				String date = utilitaires.EntreesSorties.getString("Saisissez la nouvelle date de cloture: ");
				//r.modifierDateCloture(idComp, date);
			}
		}));
		menuCompetition.ajoute(new Option("Afficher les compÃ©titions", "i", new Action() {
			public void optionSelectionnee() {
				r.getCompetition();
			}
		}));
		
		/*
		 * 
		 */
		
		// Menu Personne
		Menu menuPersonne = new Menu("Personnes", "b");
		menuPersonne.ajouteRevenir("r");
		// CrÃ©er une personne
		menuPersonne.ajoute(new Option("CrÃ©er une personne", "a", new Action() {
			public void optionSelectionnee() {
				String nom = utilitaires.EntreesSorties.getString("Saisissez le Nom: ");
				String prenom = utilitaires.EntreesSorties.getString("Saisissez le PrÃ©nom: ");
				String mail = utilitaires.EntreesSorties.getString("Saisissez le Mail: ");
				r.creerPersonne(nom, prenom, mail);
			}
		}));
		// Supprimer une personne
		menuPersonne.ajoute(new Option("Supprimer une personne", "b", new Action() {
			public void optionSelectionnee() {
				r.getPersonne();
				int idPers = utilitaires.EntreesSorties.getInt("Saisissez l'id de la personne: ");
				//TODO r.supprimerPersonne(idPers);
			}
		}));
		// Modifier le nom et le prÃ©nom
		menuPersonne.ajoute(new Option("Modifier le nom et le prénom d'une personne", "c", new Action() {
			public void optionSelectionnee() {
				r.getPersonne();
				int idPers = utilitaires.EntreesSorties.getInt("Saisissez l'id de la personne: ");
				String prenom = utilitaires.EntreesSorties.getString("Saisissez le nouveau prÃ©nom: ");
				String nom = utilitaires.EntreesSorties.getString("Saisissez le nouveau nom: ");
				r.modifierPrenom(idPers, prenom);
				r.modifierNomCandidat(idPers, nom);
				
			}
		}));
		// Modifier le mail d'une personne
		menuPersonne.ajoute(new Option("Modifier le mail d'une personne", "d", new Action() {
			public void optionSelectionnee() {
				r.getPersonne();
				int idPers = utilitaires.EntreesSorties.getInt("Saisissez l'id de la personne: ");
				String mail = utilitaires.EntreesSorties.getString("Saisissez le nouveau mail: ");
				r.modifierMail(idPers, mail);
			}
		}));
		// Affiche les Ã©quipes de la personne
		menuPersonne.ajoute(new Option("Afficher les Ã©quipes d'une personne", "e", new Action() {
			public void optionSelectionnee() {
				r.getPersonne();
				int idPers = utilitaires.EntreesSorties.getInt("Saisissez l'id de la personne : ");
				r.getEquipePersonne(idPers);
			}
		}));
		// Affiche le mail d'une personne
		menuPersonne.ajoute(new Option("Afficher le mail d'une personne", "f", new Action() {
			public void optionSelectionnee() {
				r.getPersonne();
				int idPers = utilitaires.EntreesSorties.getInt("Saisissez l'id de la personne: ");
				r.getMail(idPers);
			}
		}));
		// Afficher le prÃ©nom d'une personne
		menuPersonne.ajoute(new Option("Afficher le prÃ©nom d'une personne", "g", new Action() {
			public void optionSelectionnee() {
				r.getPersonne();
				int idPers = utilitaires.EntreesSorties.getInt("Saisissez l'id de la personne: ");
				r.getPrenom(idPers);
			}
		}));
		menuPersonne.ajoute(new Option("Ajouter une personne Ã  une compÃ©tition", "h", new Action() {
			public void optionSelectionnee() {
				r.getPersonne();
				int idCandidat = utilitaires.EntreesSorties.getInt("Saisissez l'id de la personne: ");
				r.getCompetition();
				int idComp = utilitaires.EntreesSorties.getInt("Saisissez l'id de la compÃ©tition: ");
				r.ajouterCandidatCompetition(idCandidat, idComp);
			}
		}));
		menuPersonne.ajoute(new Option("DÃ©sinscrire une personne d'une compÃ©tition", "i", new Action() {
			public void optionSelectionnee() {
				r.getPersonne();
				int idCandidat = utilitaires.EntreesSorties.getInt("Saisissez l'id du candidat: ");
				r.getCompetition();
				int idComp = utilitaires.EntreesSorties.getInt("Saisissez l'id de la compÃ©tition: ");
				r.desinscrireCandidat(idCandidat, idComp);
			}
		}));
		// Afficher les personnes
		menuPersonne.ajoute(new Option("Afficher les personnes", "j", new Action() {
			public void optionSelectionnee() {
				r.getPersonne();
			}
		}));
		
		/*
		 * 
		 */
		
		// menu Equipe
		Menu menuEquipe = new Menu("Equipes", "c");
		menuEquipe.ajouteRevenir("r");
		// Ajouter une personne dans l'Ã©quipe
		menuEquipe.ajoute(new Option("Ajouter une personne dans une Ã©quipe", "a", new Action() {
			public void optionSelectionnee() {
				r.getPersonne();
				int idCandidat = utilitaires.EntreesSorties.getInt("Saisissez l'id du candidat qui rejoindra l'Ã©quipe: ");
				r.getEquipe();
				int idEquipe = utilitaires.EntreesSorties.getInt("Saisissez l'id de l'Ã©quipe: ");
				r.ajouterPersonneEquipe(idCandidat, idEquipe);
			}
		}));
		// Supprime une Ã©quipe
		menuEquipe.ajoute(new Option("Supprimer une Ã©quipe", "b", new Action() {
			public void optionSelectionnee() {
				r.getEquipe();
				int idEquipe = utilitaires.EntreesSorties.getInt("Saisissez l'id de l'Ã©quipe: ");
				r.supprimerCandidat(idEquipe);
			}
		}));
		// Afficher les membres de l'Ã©quipe
		menuEquipe.ajoute(new Option("Afficher les membres d'une Ã©quipe", "c", new Action() {
			public void optionSelectionnee() {
				r.getEquipe();
				int idEquipe = utilitaires.EntreesSorties.getInt("Saisissez l'id de l'Ã©quipe: ");
				r.getPersonneEquipe(idEquipe);
			}
		}));
		// Supprimer un membre de l'Ã©quipe
		menuEquipe.ajoute(new Option("Supprimer un membre de l'Ã©quipe", "d", new Action() {
			public void optionSelectionnee() {
				r.getEquipe();
				int idEquipe = utilitaires.EntreesSorties.getInt("Saisissez l'id de l'Ã©quipe: ");
				r.getPersonneEquipe(idEquipe);
				int idPers = utilitaires.EntreesSorties.getInt("Saisissez l'id de la personne: ");
				r.supprimerPersonneEquipe(idEquipe, idPers);
			}
		}));
		// crÃ©er une Ã©quipe
		menuEquipe.ajoute(new Option("CrÃ©er une Ã©quipe", "e", new Action() {
			public void optionSelectionnee() {
				String nom = utilitaires.EntreesSorties.getString("Saisissez le nom de la nouvelle Ã©quipe: ");
				r.creerEquipe(nom);
			}
		}));
		menuEquipe.ajoute(new Option("Ajouter une Ã©quipe Ã  une compÃ©tition", "f", new Action() {
			public void optionSelectionnee() {
				r.getEquipe();
				int idEquipe = utilitaires.EntreesSorties.getInt("Saisissez l'id d'une Ã©quipe: ");
				r.getCompetition();
				int idComp = utilitaires.EntreesSorties.getInt("Saisissez l'id de la compÃ©tition: ");
				r.ajouterEquipeCompetition(idEquipe, idComp);
			}
		}));
		// Affiche les Ã©quipes
		menuEquipe.ajoute(new Option("Afficher les Ã©quipes", "g", new Action() {
			public void optionSelectionnee() {
				r.getEquipe();
			}
		}));
		
		
		
		// Ajout au menu princpal
		menuPrincipal.ajoute(menuCompetition);
		menuPrincipal.ajoute(menuPersonne);
		menuPrincipal.ajoute(menuEquipe);
		menuPrincipal.ajouteQuitter("q");
		
		menuPrincipal.start();
		
		try
		{
			inscriptions.sauvegarder();
		} 
		catch (IOException e)
		{
			System.out.println("Sauvegarde impossible." + e);
		}
	}
}
