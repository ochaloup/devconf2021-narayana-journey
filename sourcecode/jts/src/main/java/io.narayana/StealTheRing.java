package io.narayana;

import com.arjuna.ats.internal.jts.ORBManager;
import org.omg.CORBA.SystemException;
import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.ResourcePOA;
import org.omg.CosTransactions.Vote;

public class StealTheRing extends ResourcePOA {

    private final boolean doCommit;
    private final Resource reference;

    public StealTheRing(final boolean doCommit) {
        ORBManager.getPOA().objectIsReady(this);
        this.doCommit = doCommit;
        reference = org.omg.CosTransactions.ResourceHelper.narrow(ORBManager.getPOA().corbaReference(this));
    }

    public Resource getReference() {
        return reference;
    }

    public Vote prepare() throws SystemException {
        System.out.println("StealTheRing : in preparation");

        if (doCommit) {
            System.out.println("\tStealTheRing : VoteCommit");
            return Vote.VoteCommit;
        } else {
            System.out.println("\tStealTheRing : VoteRollback");
            return Vote.VoteRollback;
        }
    }

    public void rollback() throws SystemException {
        System.out.println("StealTheRing : rollback");
    }

    public void commit() throws SystemException {
        System.out.println("StealTheRing : commit");
    }

    public void forget() throws SystemException {
        System.out.println("StealTheRing : forget");
    }

    public void commit_one_phase() throws SystemException {
        System.out.println("StealTheRing : commit_one_phase");
    }

}
