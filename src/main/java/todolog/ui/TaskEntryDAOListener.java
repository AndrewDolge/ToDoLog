package todolog.ui;

import todolog.core.EntryDAO;
import todolog.core.TaskDAO;

public interface TaskEntryDAOListener {



    public void onTaskEntryChange(TaskDAO taskDAO, EntryDAO entryDAO);


}