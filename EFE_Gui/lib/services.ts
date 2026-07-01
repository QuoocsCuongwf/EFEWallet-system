"use client"

import { api, requestWithRetry } from "./api"
import { getUser } from "./auth-storage"

export type OtpAction = "REGISTER" | "TRANSFER"

type ApiResponse<T> = {
  success: boolean
  message?: string
  data: T
}

type LoginData = {
  token: string
  refreshToken?: string
  typeToken?: string
  expiresIn?: number
}

function splitFullName(fullName: string) {
  const parts = fullName.trim().split(/\s+/).filter(Boolean)
  if (parts.length <= 1) {
    return { firstName: parts[0] ?? "", lastName: parts[0] ?? "" }
  }
  return {
    firstName: parts.slice(0, -1).join(" "),
    lastName: parts[parts.length - 1],
  }
}

// ---------------- Auth ----------------

export async function registerDraft(input: {
  fullName: string
  email: string
  password: string
}) {
  const { firstName, lastName } = splitFullName(input.fullName)
  const { data } = await api.post<ApiResponse<unknown>>("/api/v1/auth/register", {
    email: input.email,
    password: input.password,
    firstName,
    lastName,
  })
  return data.data
}

export async function requestOtp(input: { action: OtpAction; email?: string }) {
  const identifier = input.email ?? getUser()?.email
  if (!identifier) throw new Error("Không tìm thấy email để gửi OTP.")

  const { data } = await api.post<ApiResponse<void>>("/api/v1/auth/request-otp", {
    identifier,
    action: input.action,
  })
  return data.data
}

export async function verifyRegisterOtp(input: { email: string; otp: string }) {
  const { data } = await api.post<ApiResponse<void>>("/api/v1/auth/verify-otp", {
    identifier: input.email,
    action: "REGISTER",
    otpCode: input.otp,
  })
  return data.data
}

export async function login(input: { email: string; password: string }) {
  const { data } = await api.post<ApiResponse<LoginData>>(
    "/api/v1/auth/login",
    input,
  )
  return data.data
}

// ---------------- Wallet ----------------

export type TransferInput = {
  recipient: string
  amount: number
  note?: string
  otp: string
}

/**
 * Executes a money transfer. The idempotencyKey MUST be generated once on the
 * client (UUID v4) and kept fixed across retries so the backend can dedupe and
 * never debit the account twice.
 */
export async function transfer(input: TransferInput, idempotencyKey: string) {
  const identifier = getUser()?.email
  if (!identifier) throw new Error("Không tìm thấy email người dùng để xác thực giao dịch.")

  await api.post<ApiResponse<void>>("/api/v1/auth/verify-transfer-otp", {
    identifier,
    otpCode: input.otp,
    toWalletAddress: input.recipient,
    amount: input.amount,
  })

  const response = await requestWithRetry<ApiResponse<{ status: string; transactionId?: string }>>(
    {
      url: "/api/v1/wallet/transfer",
      method: "POST",
      headers: {
        "Idempotency-Key": idempotencyKey,
      },
      data: {
        toWalletAddress: input.recipient,
        amount: input.amount,
        description: input.note,
      },
    },
    { retries: 3 },
  )
  return response.data
}
