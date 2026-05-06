@echo off
setlocal

set "ROOT=%~dp0.."
cd /d "%ROOT%\frontend"
set "VITE_API_URL=http://localhost:8081/api"
npm.cmd run dev
