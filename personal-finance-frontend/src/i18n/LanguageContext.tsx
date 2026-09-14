import { createContext, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from 'react'

export type Language = 'en' | 'vi'

const translations: Record<string, string> = {
  'Personal finance': 'Tài chính cá nhân',
  'Personal finance, clearly': 'Quản lý tài chính thật rõ ràng',
  'Know where your money goes.': 'Biết rõ tiền của bạn đã đi đâu.',
  'Track accounts, transactions and monthly budgets from one calm, private dashboard.': 'Theo dõi tài khoản, giao dịch và ngân sách tháng trong một trang tổng quan riêng tư, dễ dùng.',
  'All amounts are managed in VND.': 'Tất cả số tiền được quản lý bằng VND.',
  'Welcome back': 'Chào mừng bạn trở lại',
  'Sign in to continue to your financial dashboard.': 'Đăng nhập để tiếp tục sử dụng trang quản lý tài chính.',
  'Email': 'Email', 'Password': 'Mật khẩu', 'Your password': 'Mật khẩu của bạn',
  'Forgot password?': 'Quên mật khẩu?', 'Sign in': 'Đăng nhập', 'Signing in…': 'Đang đăng nhập…',
  'New here?': 'Bạn chưa có tài khoản?', 'Create an account': 'Tạo tài khoản',
  'Start with a clear picture': 'Bắt đầu với bức tranh rõ ràng', 'Build better money habits.': 'Xây dựng thói quen tài chính tốt hơn.',
  'Create your private workspace, then add your first account and category.': 'Tạo không gian riêng, sau đó thêm tài khoản và danh mục đầu tiên.',
  'Your account is created with the USER role.': 'Tài khoản của bạn được tạo với vai trò người dùng.',
  'Save your recovery code': 'Lưu mã khôi phục', 'Create your account': 'Tạo tài khoản',
  'This code is shown only once. Store it in a password manager before continuing.': 'Mã này chỉ hiển thị một lần. Hãy lưu lại an toàn trước khi tiếp tục.',
  'It only takes a minute to get started.': 'Bạn chỉ mất một phút để bắt đầu.',
  'If you lose both your password and this code, the app cannot recover your account.': 'Nếu mất cả mật khẩu và mã này, ứng dụng sẽ không thể khôi phục tài khoản của bạn.',
  'I saved it — continue': 'Tôi đã lưu — tiếp tục', 'Name': 'Tên', 'Your name': 'Tên của bạn',
  'Confirm password': 'Xác nhận mật khẩu', 'Repeat your password': 'Nhập lại mật khẩu',
  'Creating account…': 'Đang tạo tài khoản…', 'Create account': 'Tạo tài khoản',
  'Already have an account?': 'Đã có tài khoản?', 'Passwords do not match': 'Mật khẩu không khớp',
  'Create a new password': 'Tạo mật khẩu mới',
  'Enter your account email and private recovery code, then choose a new password.': 'Nhập email tài khoản và mã khôi phục riêng, sau đó chọn mật khẩu mới.',
  'Recovery code': 'Mã khôi phục', 'Your private recovery code': 'Mã khôi phục riêng của bạn',
  'New password': 'Mật khẩu mới', 'Confirm new password': 'Xác nhận mật khẩu mới',
  'Reset password': 'Đặt lại mật khẩu', 'Resetting…': 'Đang đặt lại…', 'Back to sign in': 'Quay lại đăng nhập',
  'Your password has been reset successfully.': 'Mật khẩu của bạn đã được đặt lại thành công.',
  'Your new personal recovery code (shown once):': 'Mã khôi phục cá nhân mới (chỉ hiển thị một lần):',
  'Save it now. Your old shared recovery code no longer works.': 'Hãy lưu ngay. Mã khôi phục cũ không còn hiệu lực.',
  'Sign in with new password': 'Đăng nhập bằng mật khẩu mới',
  'Dashboard': 'Tổng quan', 'Accounts': 'Tài khoản', 'Categories': 'Danh mục', 'Transactions': 'Giao dịch', 'Budgets': 'Ngân sách',
  'Signed in': 'Đã đăng nhập', 'Sign out': 'Đăng xuất', 'Open menu': 'Mở menu', 'Close menu': 'Đóng menu',
  'Monthly overview': 'Tổng quan tháng', 'Your financial overview for this month.': 'Tổng quan tài chính của bạn trong tháng này.',
  'Total balance': 'Tổng số dư', 'Income': 'Thu nhập', 'Expenses': 'Chi tiêu', 'Expense': 'Chi tiêu',
  'Net amount': 'Số tiền ròng', 'This month': 'Tháng này', 'Income minus expenses': 'Thu nhập trừ chi tiêu',
  'Monthly cash flow': 'Dòng tiền tháng', 'Income compared with expenses': 'So sánh thu nhập và chi tiêu',
  'Budget health': 'Tình trạng ngân sách', 'Current month by category': 'Tháng hiện tại theo danh mục',
  'No budgets this month': 'Tháng này chưa có ngân sách', 'Recent transactions': 'Giao dịch gần đây',
  'Latest activity from this month': 'Hoạt động mới nhất trong tháng', 'No transactions this month.': 'Tháng này chưa có giao dịch.',
  'Money sources': 'Nguồn tiền', 'Manage cash, bank accounts and e-wallets. All balances use VND.': 'Quản lý tiền mặt, tài khoản ngân hàng và ví điện tử. Tất cả số dư dùng VND.',
  'Add account': 'Thêm tài khoản', 'No active accounts': 'Chưa có tài khoản hoạt động',
  'Create your first account to start tracking money.': 'Tạo tài khoản đầu tiên để bắt đầu theo dõi tiền.',
  'Active': 'Hoạt động', 'Current balance · VND': 'Số dư hiện tại · VND', 'Archived accounts': 'Tài khoản đã lưu trữ',
  'Restore': 'Khôi phục', 'Edit account': 'Sửa tài khoản', 'New account': 'Tài khoản mới',
  'Currency is fixed to VND for this version.': 'Phiên bản này cố định đơn vị tiền là VND.',
  'Account name': 'Tên tài khoản', 'Type': 'Loại', 'Cash': 'Tiền mặt', 'Bank account': 'Tài khoản ngân hàng',
  'E-wallet': 'Ví điện tử', 'Initial balance': 'Số dư ban đầu', 'Currency': 'Tiền tệ',
  'Cancel': 'Hủy', 'Saving…': 'Đang lưu…', 'Save account': 'Lưu tài khoản', 'Remove account?': 'Xóa tài khoản?',
  'Accounts with transactions are archived; unused accounts may be permanently deleted.': 'Tài khoản có giao dịch sẽ được lưu trữ; tài khoản chưa dùng có thể bị xóa vĩnh viễn.',
  'Remove account': 'Xóa tài khoản', 'Edit': 'Sửa', 'Archive': 'Lưu trữ',
  'Organization': 'Phân loại', 'Separate income and expenses into clear categories for reports and budgets.': 'Phân chia thu nhập và chi tiêu thành các danh mục rõ ràng cho báo cáo và ngân sách.',
  'Add category': 'Thêm danh mục', 'No categories yet': 'Chưa có danh mục',
  'Add an income or expense category to continue.': 'Thêm danh mục thu nhập hoặc chi tiêu để tiếp tục.',
  'Income categories': 'Danh mục thu nhập', 'Expense categories': 'Danh mục chi tiêu', 'No description': 'Không có mô tả',
  'Edit category': 'Sửa danh mục', 'New category': 'Danh mục mới', 'Description': 'Mô tả', 'Save category': 'Lưu danh mục',
  'Delete category?': 'Xóa danh mục?', 'The backend may reject this if the category is still used by transactions or budgets.': 'Không thể xóa nếu danh mục vẫn đang được dùng bởi giao dịch hoặc ngân sách.',
  'Cash flow': 'Dòng tiền', 'Record income and expenses, then filter them by one dimension and an optional date range.': 'Ghi lại thu nhập và chi tiêu, sau đó lọc theo tiêu chí và khoảng ngày.',
  'Add transaction': 'Thêm giao dịch', 'Filter by': 'Lọc theo', 'All transactions': 'Tất cả giao dịch',
  'Account': 'Tài khoản', 'Category': 'Danh mục', 'Value': 'Giá trị', 'Choose…': 'Chọn…',
  'From date': 'Từ ngày', 'To date': 'Đến ngày', 'Clear': 'Xóa bộ lọc',
  'No transactions found': 'Không tìm thấy giao dịch', 'Add a transaction or change the selected filters.': 'Thêm giao dịch hoặc thay đổi bộ lọc.',
  'Transaction': 'Giao dịch', 'Date': 'Ngày', 'Amount': 'Số tiền', 'Edit transaction': 'Sửa giao dịch',
  'New transaction': 'Giao dịch mới', 'Choose account': 'Chọn tài khoản', 'Choose matching category': 'Chọn danh mục phù hợp',
  'Save transaction': 'Lưu giao dịch', 'Delete transaction?': 'Xóa giao dịch?',
  'This action permanently deletes the selected transaction.': 'Thao tác này sẽ xóa vĩnh viễn giao dịch đã chọn.',
  'Spending plan': 'Kế hoạch chi tiêu', 'Set one monthly budget for each active expense category and monitor progress.': 'Đặt ngân sách tháng cho từng danh mục chi tiêu và theo dõi tiến độ.',
  'Add budget': 'Thêm ngân sách', 'Year': 'Năm', 'Month': 'Tháng', 'No budgets for this month': 'Tháng này chưa có ngân sách',
  'Create a budget for an expense category.': 'Tạo ngân sách cho một danh mục chi tiêu.', 'Edit budget': 'Sửa ngân sách',
  'New monthly budget': 'Ngân sách tháng mới', 'Expense category': 'Danh mục chi tiêu', 'Choose category': 'Chọn danh mục',
  'Budget amount (VND)': 'Số tiền ngân sách (VND)', 'Save budget': 'Lưu ngân sách', 'Delete budget?': 'Xóa ngân sách?',
  'This removes the monthly budget. Your transactions remain unchanged.': 'Thao tác này xóa ngân sách tháng. Các giao dịch không thay đổi.',
  'Loading your data…': 'Đang tải dữ liệu…', 'Opening page…': 'Đang mở trang…', "We couldn't load this page": 'Không thể tải trang này',
  'Try again': 'Thử lại', 'Delete': 'Xóa', 'Please wait…': 'Vui lòng chờ…', 'Close': 'Đóng',
  'Show password': 'Hiện mật khẩu', 'Hide password': 'Ẩn mật khẩu', 'Show recovery code': 'Hiện mã khôi phục', 'Hide recovery code': 'Ẩn mã khôi phục',
  'Copy recovery code': 'Sao chép mã khôi phục', '8–72 characters': '8–72 ký tự',
  'January': 'Tháng 1', 'February': 'Tháng 2', 'March': 'Tháng 3', 'April': 'Tháng 4',
  'May': 'Tháng 5', 'June': 'Tháng 6', 'July': 'Tháng 7', 'August': 'Tháng 8',
  'September': 'Tháng 9', 'October': 'Tháng 10', 'November': 'Tháng 11', 'December': 'Tháng 12',
}

const patterns: Array<[RegExp, (match: RegExpMatchArray) => string]> = [
  [/^Across (\d+) active accounts$/, m => `Trên ${m[1]} tài khoản hoạt động`],
  [/^(\d+) total$/, m => `Tổng cộng ${m[1]}`],
  [/^(.+) of (.+)$/, m => `${m[1]} trên ${m[2]}`],
  [/^(.+) spent$/, m => `Đã chi ${m[1]}`],
  [/^(.+) remaining$/, m => `Còn lại ${m[1]}`],
  [/^Account #(\d+)$/, m => `Tài khoản #${m[1]}`],
  [/^Category #(\d+)$/, m => `Danh mục #${m[1]}`],
  [/^Account (created|updated) successfully$/, m => `Đã ${m[1] === 'created' ? 'tạo' : 'cập nhật'} tài khoản thành công`],
  [/^Category (created|updated) successfully$/, m => `Đã ${m[1] === 'created' ? 'tạo' : 'cập nhật'} danh mục thành công`],
  [/^Transaction (created|updated) successfully$/, m => `Đã ${m[1] === 'created' ? 'tạo' : 'cập nhật'} giao dịch thành công`],
  [/^Budget (created|updated) successfully$/, m => `Đã ${m[1] === 'created' ? 'tạo' : 'cập nhật'} ngân sách thành công`],
]

function translate(value: string) {
  const exact = translations[value]
  if (exact) return exact
  for (const [pattern, render] of patterns) {
    const match = value.match(pattern)
    if (match) return render(match)
  }
  return value
}

interface LanguageValue { language: Language; toggleLanguage: () => void }
const LanguageContext = createContext<LanguageValue | null>(null)

export function LanguageProvider({ children }: { children: ReactNode }) {
  const [language, setLanguage] = useState<Language>(() => localStorage.getItem('language') === 'vi' ? 'vi' : 'en')
  const originalText = useRef(new WeakMap<Text, string>())
  const originalAttributes = useRef(new WeakMap<Element, Map<string, string>>())

  useEffect(() => {
    localStorage.setItem('language', language)
    document.documentElement.lang = language

    const translateTextNode = (node: Text) => {
      let original = originalText.current.get(node)
      const current = node.nodeValue ?? ''
      if (original === undefined) {
        original = current
        originalText.current.set(node, original)
      } else if (language === 'en' && current !== original) {
        original = current
        originalText.current.set(node, original)
      } else if (language === 'vi' && current !== original && current !== replaceKeepingWhitespace(original, translate(original.trim()))) {
        original = current
        originalText.current.set(node, original)
      }
      const next = language === 'vi' ? replaceKeepingWhitespace(original, translate(original.trim())) : original
      if (current !== next) node.nodeValue = next
    }

    const translateElement = (element: Element) => {
      const names = ['placeholder', 'aria-label', 'title']
      let saved = originalAttributes.current.get(element)
      if (!saved) { saved = new Map(); originalAttributes.current.set(element, saved) }
      for (const name of names) {
        const current = element.getAttribute(name)
        if (current === null) continue
        if (!saved.has(name)) saved.set(name, current)
        const original = saved.get(name)!
        const next = language === 'vi' ? translate(original) : original
        if (current !== next) element.setAttribute(name, next)
      }
    }

    const translateTree = (root: Node) => {
      if (root.nodeType === Node.TEXT_NODE) translateTextNode(root as Text)
      if (root.nodeType === Node.ELEMENT_NODE) translateElement(root as Element)
      const walker = document.createTreeWalker(root, NodeFilter.SHOW_ELEMENT | NodeFilter.SHOW_TEXT)
      while (walker.nextNode()) {
        const node = walker.currentNode
        if (node.nodeType === Node.TEXT_NODE) translateTextNode(node as Text)
        else translateElement(node as Element)
      }
    }

    translateTree(document.body)
    const observer = new MutationObserver(mutations => {
      for (const mutation of mutations) {
        if (mutation.type === 'characterData') translateTextNode(mutation.target as Text)
        for (const node of mutation.addedNodes) translateTree(node)
      }
    })
    observer.observe(document.body, { childList: true, characterData: true, subtree: true })
    return () => observer.disconnect()
  }, [language])

  const value = useMemo(() => ({ language, toggleLanguage: () => setLanguage(current => current === 'en' ? 'vi' : 'en') }), [language])
  return <LanguageContext.Provider value={value}>{children}</LanguageContext.Provider>
}

function replaceKeepingWhitespace(original: string, translated: string) {
  if (!original.trim()) return original
  const start = original.match(/^\s*/)?.[0] ?? ''
  const end = original.match(/\s*$/)?.[0] ?? ''
  return `${start}${translated}${end}`
}

export function useLanguage() {
  const context = useContext(LanguageContext)
  if (!context) throw new Error('useLanguage must be used inside LanguageProvider')
  return context
}

export function useTranslation() {
  const { language } = useLanguage()
  return (value: string) => language === 'vi' ? translate(value) : value
}
