package bolts;

import bolts.Task.UnobservedExceptionHandler;

class UnobservedErrorNotifier {
    private Task<?> task;

    public UnobservedErrorNotifier(Task<?> task) {
        this.task = task;
    }

    /* Access modifiers changed, original: protected */
    public void finalize() throws Throwable {
        try {
            Task task = this.task;
            if (task != null) {
                UnobservedExceptionHandler unobservedExceptionHandler = Task.getUnobservedExceptionHandler();
                if (unobservedExceptionHandler != null) {
                    unobservedExceptionHandler.unobservedException(task, new UnobservedTaskException(task.getError()));
                }
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public void setObserved() {
        this.task = null;
    }
}
