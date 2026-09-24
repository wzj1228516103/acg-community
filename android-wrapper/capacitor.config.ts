import type { CapacitorConfig } from '@capacitor/cli'

const config: CapacitorConfig = {
  appId: 'com.acg.community',
  appName: '漫化 ACG 社区',
  webDir: '../acg-frontend/dist',
  server: {
    url: 'http://39.105.128.249',
    cleartext: true,
    androidScheme: 'http',
  },
}

export default config
