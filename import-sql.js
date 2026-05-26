const mysql = require('mysql2/promise');
const fs = require('fs');
const path = require('path');

async function main() {
  const sqlFile = path.join(__dirname, 'acg-backend/src/main/resources/db/schema.sql');
  const rawSql = fs.readFileSync(sqlFile, 'utf8');

  console.log('正在连接数据库 8.148.195.220:3306/acg_community ...');

  const connection = await mysql.createConnection({
    host: '8.148.195.220',
    port: 3306,
    user: 'acg_community',
    password: 'tfiwSpTLSYBK27Yi',
    database: 'acg_community',
    multipleStatements: true,
    charset: 'utf8mb4'
  });

  console.log('数据库连接成功!');

  // 去掉 CREATE DATABASE 和 USE 语句
  let sql = rawSql
    .replace(/CREATE DATABASE[^;]*;/gi, '')
    .replace(/USE\s+`?acg_community`?\s*;/gi, '');

  console.log('清理并执行 SQL...\n');

  try {
    const [results] = await connection.query(sql);
    if (Array.isArray(results)) {
      for (let i = 0; i < results.length; i++) {
        const r = results[i];
        if (r.affectedRows !== undefined) {
          console.log('  语句 ' + (i + 1) + ': 影响 ' + r.affectedRows + ' 行');
        } else if (r.warningCount !== undefined) {
          console.log('  语句 ' + (i + 1) + ': OK (warnings: ' + r.warningCount + ')');
        }
      }
      console.log('\n所有语句执行完成，共 ' + results.length + ' 条结果');
    } else {
      console.log('执行结果:', results);
    }
  } catch (err) {
    console.error('执行出错:', err.message);
  }

  // 验证导入结果
  console.log('\n========== 导入结果验证 ==========');
  const tables = [
    't_user', 't_category', 't_product', 't_order', 't_order_item',
    't_makeup_service', 't_makeup_booking', 't_makeup_artist_application',
    't_merchant_application', 't_chat_room', 't_message', 't_favorite'
  ];

  for (const table of tables) {
    try {
      const [rows] = await connection.execute('SELECT COUNT(*) as count FROM `' + table + '`');
      console.log('  ' + table + ': ' + rows[0].count + ' 条记录');
    } catch (err) {
      console.log('  ' + table + ': 表不存在 - ' + err.message);
    }
  }

  await connection.end();
  console.log('\n完成!');
}

main().catch(err => {
  console.error('连接失败:', err.message);
  process.exit(1);
});
