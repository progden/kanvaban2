import '@testing-library/jest-dom/vitest'
import { cleanup } from '@testing-library/react'
import { afterEach } from 'vitest'

// test.globals 未開啟，@testing-library/react 偵測不到全域 afterEach，需要手動註冊每個測試後清除 DOM。
afterEach(() => {
  cleanup()
})
