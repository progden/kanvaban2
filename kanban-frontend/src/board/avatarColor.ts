// 頭像底色：依 .dev/ui-prototype/Main.dc.html／AssigneePicker.dc.html 取樣的色票，
// 依 user id 做簡單雜湊選色，同一人在整個看板顯示一致的顏色。
const PALETTE = ['#1F4BD8', '#7A4FC4', '#0F766E', '#B0561A', '#157F3D'];

export function avatarColorFor(id: string): string {
  let hash = 0;
  for (let i = 0; i < id.length; i += 1) {
    hash = (hash * 31 + id.charCodeAt(i)) >>> 0;
  }
  return PALETTE[hash % PALETTE.length];
}
