@echo off
title PfunaMati - Sistema de Gestao de Abastecimento de Agua
cd /d "%~dp0"

echo ========================================================
echo    Iniciando PfunaMati (Gestao de Abastecimento de Agua)
echo ========================================================
echo.

:: Verificar se o MySQL do XAMPP esta a correr
echo [1/3] A verificar ligacao com o MySQL...
"C:\xampp\mysql\bin\mysql.exe" -u root -e "SELECT 1;" >nul 2>&1
if %errorlevel% neq 0 (
    echo [!] MySQL nao esta ativo. A iniciar o MySQL do XAMPP em segundo plano...
    start "" /b "C:\xampp\mysql\bin\mysqld.exe" --defaults-file="C:\xampp\mysql\bin\my.ini" --standalone
    timeout /t 3 /nobreak >nul
) else (
    echo [OK] MySQL ja se encontra em execucao.
)

:: Abrir o navegador automaticamente apos 8 segundos
start "" cmd /c "timeout /t 8 /nobreak >nul && start http://localhost:8080"

:: Iniciar o Spring Boot
echo [2/3] A iniciar o servidor Spring Boot na porta 8080...
echo [3/3] A aplicacao abrira no navegador em http://localhost:8080
echo.
echo Pressione Ctrl+C para encerrar o servidor quando terminar.
echo.

call .\mvnw.cmd spring-boot:run
pause
