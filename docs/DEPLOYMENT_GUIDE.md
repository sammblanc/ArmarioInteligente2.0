... shell ...
\`\`\`

### 2. Firewall

\`\`\`bash
# UFW (Ubuntu)
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw deny 8080/tcp  # Bloquear acesso direto à aplicação
sudo ufw enable
\`\`\`

### 3. Variáveis de Ambiente Seguras

\`\`\`bash
# Usar arquivo .env (não commitado)
echo "DATABASE_PASSWORD=senha_super_segura" > .env
echo "JWT_SECRET=jwt_secret_muito_longo_e_complexo" >> .env

# Carregar no Docker Compose
docker-compose --env-file .env up -d
\`\`\`

## 🚨 Troubleshooting

### Problemas Comuns

#### 1. Aplicação não inicia

\`\`\`bash
# Verificar logs
docker-compose logs app

# Verificar configurações
docker-compose config

# Verificar conectividade do banco
docker-compose exec app nc -zv db 5432
\`\`\`

#### 2. Erro de conexão com banco

\`\`\`bash
# Verificar se PostgreSQL está rodando
docker-compose ps

# Testar conexão manual
docker-compose exec db psql -U armariointeligente -d armariointeligente
\`\`\`

#### 3. Performance baixa

\`\`\`bash
# Verificar recursos
docker stats

# Verificar logs de performance
grep "slow" logs/armario-inteligente.log

# Ajustar configurações JVM
export JAVA_OPTS="-Xmx2g -Xms1g"
\`\`\`

---

## 🎯 Próximos Passos

Após o deploy bem-sucedido:

1. ✅ Configurar monitoramento contínuo
2. ✅ Implementar CI/CD pipeline
3. ✅ Configurar alertas automáticos
4. ✅ Documentar procedimentos operacionais
5. ✅ Treinar equipe de operações
