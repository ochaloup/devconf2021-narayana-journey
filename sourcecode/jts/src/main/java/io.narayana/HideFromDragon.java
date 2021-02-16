package io.narayana;

import com.arjuna.ats.internal.jts.ORBManager;
import org.omg.CORBA.SystemException;
import org.omg.CosTransactions.HeuristicCommit;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.HeuristicRollback;
import org.omg.CosTransactions.NotPrepared;
import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.ResourcePOA;
import org.omg.CosTransactions.Vote;

public class HideFromDragon extends ResourcePOA {

    private final boolean doCommit;
    private final Resource reference;

    public HideFromDragon(final boolean doCommit) {
        ORBManager.getPOA().objectIsReady(this);
        this.doCommit = doCommit;
        reference = org.omg.CosTransactions.ResourceHelper.narrow(ORBManager.getPOA().corbaReference(this));
    }

    public Resource getReference() {
        return reference;
    }

    public org.omg.CosTransactions.Vote prepare() throws SystemException {
        System.out.println("HideFromDragon : in preparation");

        if (doCommit) {
            System.out.println("\tHideFromDragon : VoteCommit");
            return Vote.VoteCommit;
        } else {
            System.out.println("\tHideFromDragon : VoteRollback");
            return Vote.VoteRollback;
        }
    }

    public void rollback() throws SystemException {
        System.out.println("HideFromDragon : rollback");
    }

    public void commit() throws SystemException {
        System.out.println("HideFromDragon : commit");
    }

    public void forget() throws SystemException {
        System.out.println("HideFromDragon : forget");
    }

    public void commit_one_phase() throws SystemException {
        System.out.println("HideFromDragon : commit_one_phase");
    }

}
