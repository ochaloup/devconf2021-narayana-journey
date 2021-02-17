package cz.devconf2021.stm;

import org.jboss.stm.annotations.NestedTopLevel;
import org.jboss.stm.annotations.Transactional;

@Transactional
@NestedTopLevel
public interface LockpickingTransactionalService {
    int getLockpickingAction();
    void addLockpickingAction(String details);
}
