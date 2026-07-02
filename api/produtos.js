// api/produtos.js
import mysql from 'mysql2/promise';

export default async function handler(req, res) {
  // Garante tratamento de cabeçalhos de CORS se necessário
  res.setHeader('Access-Control-Allow-Origin', '*');
  
  let connection;
  try {
    // Configura a conexão usando a variável que você vai preencher na Vercel
    connection = await mysql.createConnection(process.env.MYSQL_URL);

    // Seleciona os itens do banco que não estão marcados como vendidos (ajuste os nomes das colunas se precisar)
    const [rows] = await connection.execute(
      'SELECT id, nome, preco FROM estoque WHERE vendido = 0'
    );
    
    await connection.end();
    return res.status(200).json(rows);
  } catch (error) {
    if (connection) await connection.end();
    return res.status(500).json({ error: error.message });
  }
}
