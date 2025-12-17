public abstract class AbstractPhone {

    private int year;
    private String company;
    public AbstractPhone (int year) {
        this.year = year;
        this.company = company;
    }
    private void openConnection(){
        //findComutator
        //openNewConnection...
    }
    public void call(int number) {
        openConnection();
        System.out.println("Вызываю номер");
    }

    public void ring() {
        System.out.println("Дзынь-дзынь");
    }

    public abstract void ring(int inputNumber);
}
