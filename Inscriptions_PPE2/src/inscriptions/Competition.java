package inscriptions;

import java.io.Serializable;
import java.util.Collections;
import java.time.LocalDate;
import java.util.Set;
import java.util.TreeSet;

import DB.Requete;

/**
 * Représente une compétition, c'est-à-dire un ensemble de candidats 
 * inscrits à un événement, les inscriptions sont closes à la date dateCloture.
 *
 */

public class Competition implements Comparable<Competition>, Serializable
{
	private static final long serialVersionUID = -2882150118573759729L;
	private Inscriptions inscriptions;
	private int id;
	private String nom;
	private Set<Candidat> candidats;
	private LocalDate dateCloture;
	private boolean enEquipe = false;

	Competition(Inscriptions inscriptions, int id, String nom, LocalDate dateCloture, boolean enEquipe)
	{
		this.enEquipe = enEquipe;
		this.inscriptions = inscriptions;
		this.id = id;
		this.nom = nom;
		this.dateCloture = dateCloture;
		candidats = new TreeSet<>();
	}
	
	/**
	 * Retourne l'ID de la compétition.
	 * @return
	 */
	
	public int getId()
	{
		return id;
	}
	
	/**
	 * Retourne le nom de la compétition.
	 * @return
	 */
	
	public String getNom()
	{
		return nom;
	}
	
	/**
	 * Modifie le nom de la compétition.
	 */
	
	public void setNom(String nom)
	{
		Requete r = new Requete();
		r.modifierNomCompetition(id, nom);
		
		this.nom = nom ;
	}
	
	/**
	 * Retourne vrai si les inscriptions sont encore ouvertes, 
	 * faux si les inscriptions sont closes, suivant la date système.
	 * @return
	 */
	
	public boolean inscriptionsOuvertes()
	{
		return (dateCloture.compareTo(LocalDate.now()) > 0);
	}
	
	/**
	 * Retourne la date de cloture des inscriptions.
	 * @return
	 */
	
	public LocalDate getDateCloture()
	{
		return dateCloture;
	}
	
	/**
	 * Est vrai si et seulement si les inscriptions sont réservées aux équipes.
	 * @return
	 */
	
	public boolean estEnEquipe()
	{
		return enEquipe;
	}
	
	/**
	 * Modifie la date de cloture des inscriptions. Il est possible de la reculer 
	 * mais pas de l'avancer.
	 * @param dateCloture
	 */
	
	public void setDateCloture(LocalDate dateCloture)
	{		
		if(this.dateCloture.compareTo(dateCloture) < 0)
		{
			Requete r = new Requete();
			r.modifierDateCloture(id, dateCloture);
			
			this.dateCloture = dateCloture;
		}
	}
	
	/**
	 * Retourne l'ensemble des candidats inscrits.
	 * @return
	 */
	
	public Set<Candidat> getCandidats()
	{
		return Collections.unmodifiableSet(candidats);
	}
	
	/**
	 * Inscrit un candidat de type Personne à la compétition. Provoque une
	 * exception si la compétition est réservée aux équipes ou que les 
	 * inscriptions sont closes.
	 * @param personne
	 * @return
	 */
	
	public boolean add(Personne personne)
	{
		if (enEquipe || !inscriptionsOuvertes())
			throw new RuntimeException();
		personne.add(this);
		return candidats.add(personne);
	}

	/**
	 * Inscrit un candidat de type Equipe à la compétition. Provoque une
	 * exception si la compétition est réservée aux personnes ou que 
	 * les inscriptions sont closes.
	 * @param personne
	 * @return
	 */

	public boolean add(Equipe equipe)
	{
		if (!enEquipe || !inscriptionsOuvertes())
			throw new RuntimeException();
		equipe.add(this);
		return candidats.add(equipe);
	}

	/**
	 * Désinscrit un candidat.
	 * @param candidat
	 * @return
	 */
	
	public boolean remove(Candidat candidat)
	{
		candidat.remove(this);
		return candidats.remove(candidat);
	}
	
	/**
	 * Supprime la compétition de l'application.
	 */
	
	public void delete()
	{
		Requete r = new Requete();
		r.supprimerCompetition(id);
		
		for (Candidat candidat : candidats)
			remove(candidat);
		inscriptions.remove(this);
	}
	
	@Override
	public int compareTo(Competition o)
	{
		return getNom().compareTo(o.getNom());
	}
	
	@Override
	public String toString()
	{
		return getNom();
	}
}
