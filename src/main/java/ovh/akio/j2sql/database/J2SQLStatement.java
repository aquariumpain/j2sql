package ovh.akio.j2sql.database;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class J2SQLStatement {

    private int index = 1;
    private PreparedStatement preparedStatement;

    public boolean close(){
        index = 1;
        try {
            if(this.preparedStatement != null)
                preparedStatement.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public J2SQLStatement open(PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
        return this;
    }

    private void set(Object o) throws SQLException {
        if (this.preparedStatement == null)
            throw new SQLException("[J2SQL] Unable to set argument : No statement defined.");

        if (o instanceof String) {
            this.preparedStatement.setString(index, ((String) o));
        } else if (o instanceof Integer) {
            this.preparedStatement.setInt(index, ((Integer) o));
        } else if (o instanceof Float) {
            this.preparedStatement.setFloat(index, ((Float) o));
        } else if (o instanceof Double) {
            this.preparedStatement.setDouble(index, ((Double) o));
        } else if (o instanceof Long) {
            this.preparedStatement.setLong(index, ((Long) o));
        } else if (o instanceof Boolean) {
            this.preparedStatement.setBoolean(index, ((Boolean) o));
        } else {
            throw new SQLException("[J2SQL] Unable to set argument : Unknow data type. [" + o.getClass().getName() + "]");
        }
    }

    public void set(Object... o) throws SQLException {
        for (Object o1 : o) {
            this.set(o1);
            index++;
        }
    }

    public boolean execute() throws SQLException{
        this.preparedStatement.execute();
        return true;
    }

    public ResultSet getResult() throws SQLException {
        return this.preparedStatement.executeQuery();
    }

}
