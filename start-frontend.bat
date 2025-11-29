@echo off
pushd frontend
if not exist node_modules (
  npm install
)
npm run dev -- --host
popd
