package bri.serveur.service;

import java.lang.reflect.Constructor;
import java.net.Socket;

import bri.Connexion;
import bri.serveur.Console;
import bri.serveur.IService;
import bri.serveur.IUtilisateur;

/**
 * Conteneur de service BRI.
 */
public class Service implements IService
{
    /** Chargeur de classe distante. */
    private ServiceClassLoader loader;
    
    /** Nom du service. */
    private String nom;
    @Override
    public final String nom() { return this.nom; }
    @Override
    public final String toString() 
    { return "[" + (this.actif ? '*' : ' ') + "] " + this.auteur.pseudo() + '/' + nom; }

    /** Auteur du service. */
    private IUtilisateur auteur;
    @Override
    public final IUtilisateur auteur() { return this.auteur; }

    /**
     * Construction d'un nouveau conteneur de service.
     * @param auteur Auteur du service.
     * @param nom Nom du service.
     */
    public Service(final IUtilisateur auteur, final String nom)
    {
        this.nom = nom;
        this.auteur = auteur;
        this.actif = false;
        this.loader = null;
    }

    /** Drapeau d'activité du service. */
    private boolean actif;
    @Override
    public final boolean actif() { return this.actif; }
    @Override
    public final boolean activer() 
    { 
        if (!this.actif)
        {
            if (this.loader == null)
                if (!this.mettre_a_jour()) return false;
            this.actif = true;
            Console.afficher(this, "Service activé.");
            return true;
        }
        return false;
    }
    @Override
    public final boolean desactiver()
    {
        if (this.actif)
        {
            this.actif = false;
            Console.afficher(this, "Service désactivé.");
            return true;
        }
        return false;
    }

    @Override
    public final String classe() { return this.auteur.pseudo() + '.' + this.nom; }

    /**
     * Charge la classe du service BRI depuis le serveur FTP de l'auteur.
     * @throws ClassNotFoundException
     * @throws ClassFormatError
     */
    private final void charger_service_distant() throws ClassNotFoundException, ClassFormatError
    {
        final String nom_classe = this.classe();
        ServiceClassLoader loader = null;
        Class<?> classe = null;
        loader = new ServiceClassLoader(this.auteur, this.nom);
        try { classe = loader.loadClass(nom_classe); }
        catch (ClassNotFoundException e) 
        {
            loader = new ServiceClassLoader(this.auteur);
            try { classe = loader.loadClass(nom_classe); }
            catch (ClassNotFoundException e2)
            { throw new ClassNotFoundException("la classe est introuvable sur le serveur.", e2); }
        }
        if (IServiceBRI.verifier_norme(classe))
        {
            try { classe.asSubclass(IServiceBRI.class); }
            catch (ClassCastException e) { throw new ClassFormatError("impossible d'importer la classe en tant que service BRI."); }
            this.loader = loader;
        }
        else throw new ClassFormatError("la classe ne respecte pas la norme BRI.");
    }

    @Override
    public final boolean mettre_a_jour()
    {
        Console.afficher(this, "Mise à jour depuis le serveur FTP...");
        try 
        { 
            this.charger_service_distant(); 
            Console.afficher(this, "Classe du service mise à jour.");
            return true;
        }
        catch (ClassNotFoundException|ClassFormatError e)
        {
            Console.afficher(this, "| ERREUR | Impossible de charger la classe distante : " + e.getMessage());
            return false;
        }
    }

    @Override
    public final IServiceBRI nouvelle_instance(Connexion connexion)
    {
        try 
        {
            Class<? extends IServiceBRI> classe = this.loader.loadClass(this.classe()).asSubclass(IServiceBRI.class);
            Constructor<?> c = null;
            IServiceBRI service = null;
            try 
            { 
                c = classe.getDeclaredConstructor(Connexion.class); 
                service = (IServiceBRI)c.newInstance(connexion);
            }
            catch (NoSuchMethodException e)
            { 
                c = classe.getDeclaredConstructor(Socket.class);
                service = (IServiceBRI)c.newInstance(connexion.socket());
            }
            Console.afficher(this, "Nouvelle instance du service créée.");
            return service;
        }
        catch (Exception e)
        {
            Console.afficher(this, "| ERREUR | Impossible d'instancier le service : " + e.getMessage());
            return null;
        }
    }
}
