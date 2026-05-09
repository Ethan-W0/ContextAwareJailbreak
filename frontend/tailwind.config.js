/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx,vue}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['"LXGW WenKai"', '"ZCOOL WenYi"', '"Yang Rendong ZhuShi"', '"Noto Serif SC"', 'serif'],
        display: ['"LXGW WenKai"', '"ZCOOL WenYi"', 'serif'],
        // 企业级科技风无衬线字体栈，专用于合规/产品页面
        tech: [
          '"Inter"',
          '-apple-system',
          'BlinkMacSystemFont',
          '"PingFang SC"',
          '"Microsoft YaHei"',
          '"Helvetica Neue"',
          'Arial',
          'sans-serif',
        ],
      },
      colors: {
        // 品牌科技蓝
        tech: {
          50: '#f0f5ff',
          100: '#dbe7ff',
          200: '#bdd2ff',
          300: '#8fb5ff',
          400: '#5a8eff',
          500: '#165DFF',
          600: '#0f4de6',
          700: '#0c3ebf',
          800: '#0e3499',
          900: '#112f7a',
        },
        // 低饱和蓝灰
        primary: {
          50: '#f4f7fa',
          100: '#e6ecf3',
          200: '#cbd9e8',
          300: '#a3bdd6',
          400: '#759bc0',
          500: '#557eaa',
          600: '#42658f',
          700: '#375274',
          800: '#314661',
          900: '#2d3d52',
        },
        // 浅藕粉/暖灰点缀
        accent: {
          50: '#fdf6f5',
          100: '#f9ece9',
          200: '#f3d8d2',
          300: '#e9bbb0',
          400: '#dc9585',
          500: '#cd7562',
          600: '#b85c49',
          700: '#9a4a3c',
          800: '#804036',
          900: '#6c3932',
        },
        // 浅绿成功态
        sage: {
          50: '#f5f8f5',
          100: '#e8efe7',
          200: '#d1dfcf',
          300: '#aec7aa',
          400: '#87a982',
          500: '#688f63',
          600: '#52744d',
          700: '#425d3f',
          800: '#384c35',
          900: '#2f3f2d',
        },
        // 中性灰（条款卡片等）
        neutral: {
          50: '#fafbfc',
          100: '#f5f6f8',
          200: '#edf0f3',
          300: '#dce0e5',
          400: '#b0b8c1',
          500: '#88929e',
          600: '#6b7583',
          700: '#565e6b',
          800: '#474e58',
          900: '#3b4148',
        },
      },
      borderRadius: {
        soft: '8px',
        pill: '4px',
      },
      boxShadow: {
        soft: '0 1px 3px rgba(0,0,0,0.04), 0 1px 2px rgba(0,0,0,0.03)',
        card: '0 2px 8px rgba(0,0,0,0.05), 0 1px 3px rgba(0,0,0,0.03)',
        elevated: '0 4px 16px rgba(0,0,0,0.06), 0 2px 6px rgba(0,0,0,0.04)',
      },
      animation: {
        'fade-in': 'fadeIn 0.4s ease-out',
        'slide-up': 'slideUp 0.35s ease-out',
        'pulse-soft': 'pulseSoft 2s ease-in-out infinite',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%': { opacity: '0', transform: 'translateY(12px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        pulseSoft: {
          '0%, 100%': { opacity: '1' },
          '50%': { opacity: '0.6' },
        },
      },
    },
  },
  plugins: [],
};
