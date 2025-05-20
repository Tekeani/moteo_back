package moteo_back

import java.sql.Connection
import java.sql.DriverManager

object Database {
    private const val jdbcUrl = "jdbc:postgresql://ep-autumn-river-ab8ox58o-pooler.eu-west-2.aws.neon.tech/moteo?sslmode=require"
    private const val username = "moteo_owner"
    private const val password = "npg_Xrad9cAhfjo7"

    fun connect(): Connection {
        return DriverManager.getConnection(jdbcUrl, username, password)
    }
}

