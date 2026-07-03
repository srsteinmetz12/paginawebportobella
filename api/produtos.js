// api/produtos.js - Versão com fallback para dados fixos
export default async function handler(req, res) {
  // Habilita CORS
  res.setHeader('Access-Control-Allow-Origin', '*');
  
  try {
    // Tenta usar o MySQL se estiver disponível
    let mysql;
    try {
      mysql = await import('mysql2/promise');
    } catch (e) {
      // MySQL não está instalado - usa dados fixos
      console.warn('mysql2 não encontrado - usando dados de exemplo');
      return res.status(200).json(getProdutosExemplo());
    }

    // Se chegou aqui, o mysql2 está instalado
    const connection = await mysql.createConnection(process.env.MYSQL_URL);
    const [rows] = await connection.execute(
      'SELECT id, nome, preco FROM estoque WHERE vendido = 0'
    );
    await connection.end();
    
    return res.status(200).json(rows);
    
  } catch (error) {
    // Em caso de erro, retorna dados de exemplo
    console.error('Erro na API:', error.message);
    return res.status(200).json(getProdutosExemplo());
  }
}

// ✅ DADOS DE EXEMPLO (funciona SEMPRE)
function getProdutosExemplo() {
  return [
    { id: 1, nome: 'Vestido Floral', preco: 89.90 },
    { id: 2, nome: 'Calça Jeans', preco: 120.00 },
    { id: 3, nome: 'Blusa de Tricô', preco: 65.50 },
    { id: 4, nome: 'Saia Midi', preco: 75.00 },
    { id: 5, nome: 'Blazer Social', preco: 150.00 }
  ];
}