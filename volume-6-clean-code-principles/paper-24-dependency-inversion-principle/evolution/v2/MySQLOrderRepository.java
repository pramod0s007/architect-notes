public class MySQLOrderRepository {

    public void save(Order order) {
        System.out.println("MySQL: saved order ID " + order.getId()
                + " (total=$" + order.getTotal() + ")");
    }
}
