package common.ddd;

//interfaccia che rappresenta un aggregato (insieme di entità correlate)
public interface Aggregate<T> extends Entity<T>{

    T getId();

}
