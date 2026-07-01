"use client"

import { useState } from "react"
import { WindowFrame } from "@/components/window-frame"
import { LoginScreen } from "@/components/auth/login-screen"
import { RegisterScreen } from "@/components/auth/register-screen"
import { RegisterOtpScreen } from "@/components/auth/register-otp-screen"
import { Dashboard } from "@/components/wallet/dashboard"
import { TransferScreen } from "@/components/wallet/transfer-screen"
import { clearToken } from "@/lib/auth-storage"

type Screen = "login" | "register" | "register-otp" | "dashboard" | "transfer"

type User = { fullName: string; email: string }

type Transaction = {
  id: string
  recipient: string
  amount: number
  note?: string
  at: string
}

const INITIAL_BALANCE = 12_500_000

export function WalletApp() {
  const [screen, setScreen] = useState<Screen>("login")
  const [user, setUser] = useState<User | null>(null)
  const [pendingRegister, setPendingRegister] = useState<User | null>(null)
  const [balance, setBalance] = useState(INITIAL_BALANCE)
  const [transactions, setTransactions] = useState<Transaction[]>([])

  function handleLogout() {
    clearToken()
    setUser(null)
    setTransactions([])
    setBalance(INITIAL_BALANCE)
    setScreen("login")
  }

  return (
    <WindowFrame>
      {screen === "login" && (
        <LoginScreen
          prefillEmail={pendingRegister?.email}
          onLoggedIn={(u) => {
            setUser(u)
            setScreen("dashboard")
          }}
          onGoToRegister={() => setScreen("register")}
        />
      )}

      {screen === "register" && (
        <RegisterScreen
          onDraftSaved={(input) => {
            setPendingRegister(input)
            setScreen("register-otp")
          }}
          onGoToLogin={() => setScreen("login")}
        />
      )}

      {screen === "register-otp" && pendingRegister && (
        <RegisterOtpScreen
          email={pendingRegister.email}
          onVerified={() => setScreen("login")}
          onBack={() => setScreen("register")}
        />
      )}

      {screen === "dashboard" && user && (
        <Dashboard
          user={user}
          balance={balance}
          transactions={transactions}
          onTransfer={() => setScreen("transfer")}
          onLogout={handleLogout}
        />
      )}

      {screen === "transfer" && user && (
        <TransferScreen
          balance={balance}
          onBack={() => setScreen("dashboard")}
          onCompleted={(tx) => {
            setBalance((b) => b - tx.amount)
            setTransactions((list) => [
              {
                ...tx,
                at: new Date().toLocaleString("vi-VN", {
                  hour: "2-digit",
                  minute: "2-digit",
                  day: "2-digit",
                  month: "2-digit",
                }),
              },
              ...list,
            ])
            setScreen("dashboard")
          }}
        />
      )}
    </WindowFrame>
  )
}
