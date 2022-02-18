package bri.serveur.apps.session.actions;

import bri.Connexion;
import bri.serveur.apps.ISession;

public class ChangerAdresseFTP extends Action
{
    @Override
    public final String nom() { return "Changer votre adresse FTP"; }

    public ChangerAdresseFTP(ISession parent) { super(parent); }

    @Override
    public final boolean executer(Connexion connexion, String[] arguments)
    {
        // TODO Action de changement de l'adresse FTP 
        connexion.ecrire("ERREUR : Action non implémentée sur le serveur.");
        return true;
    }
}
