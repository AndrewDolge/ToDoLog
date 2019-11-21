
package todolog.core;

/**
 * Factory class to serve EntryDAO and TaskDAO implementations.
 * 
 */
public class ToDoLogFactory{

    private JDBCDAOimpl impl;

    public ToDoLogFactory(String url){

        impl = new JDBCDAOimpl(url);
    }

    public EntryDAO getEntryDAO(){
        return impl;
    }

    public TaskDAO getTaskDAO(){
        return impl;
    }

}//class