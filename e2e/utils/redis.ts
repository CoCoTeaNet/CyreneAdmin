/**
 * 轻量 Redis RESP 读取工具（零依赖，Node 原生 net 实现）
 *
 * 用途：登录链路需要真实图形验证码，后端把验证码文本写入 Redis
 * （key: VERIFY_CODE_LOGIN_{captchaId}, db 与 app-test.yml 的 myapp.rd1 一致）。
 * e2e 通过本工具读取验证码后走完整登录链路，与生产前端行为一致。
 */
import net from 'net';
import { TEST_REDIS_HOST, TEST_REDIS_PORT, TEST_REDIS_DB } from './config';

function encodeCommand(cmd: string[]): Buffer {
  const parts = [`*${cmd.length}\r\n`];
  for (const c of cmd) {
    parts.push(`$${Buffer.byteLength(c, 'utf8')}\r\n${c}\r\n`);
  }
  return Buffer.from(parts.join(''), 'utf8');
}

/**
 * 解析 N 个 RESP 简单/整型/bulk 响应；数据不足时返回 null（等待更多数据）
 */
function decodeReplies(buf: Buffer, count: number): Array<string | null> | null {
  const text = buf.toString('utf8');
  const out: Array<string | null> = [];
  let pos = 0;
  for (let i = 0; i < count; i++) {
    const nl = text.indexOf('\r\n', pos);
    if (nl === -1) return null;
    const head = text.slice(pos, nl);
    pos = nl + 2;
    if (head.startsWith('+') || head.startsWith('-') || head.startsWith(':')) {
      out.push(head.slice(1));
    } else if (head.startsWith('$')) {
      const len = parseInt(head.slice(1), 10);
      if (len === -1) {
        out.push(null);
        continue;
      }
      if (buf.length < pos + len + 2) return null;
      out.push(text.slice(pos, pos + len));
      pos += len + 2;
    } else {
      return null;
    }
  }
  return out;
}

export function redisGet(key: string): Promise<string | null> {
  return new Promise((resolve, reject) => {
    const sock = net.connect({ host: TEST_REDIS_HOST, port: TEST_REDIS_PORT });
    let buf = Buffer.alloc(0);
    const timer = setTimeout(() => {
      sock.destroy();
      reject(new Error(`Redis 读取超时: ${key}`));
    }, 5000);

    sock.on('connect', () => {
      sock.write(encodeCommand(['SELECT', TEST_REDIS_DB]));
      sock.write(encodeCommand(['GET', key]));
    });
    sock.on('data', (d) => {
      buf = Buffer.concat([buf, d]);
      const replies = decodeReplies(buf, 2);
      if (replies) {
        clearTimeout(timer);
        sock.end();
        resolve(replies[1]);
      }
    });
    sock.on('error', (e) => {
      clearTimeout(timer);
      reject(e);
    });
  });
}

/** 读取登录图形验证码（Redis db 与后端配置一致） */
export async function getVerifyCode(captchaId: string): Promise<string> {
  const code = await redisGet(`VERIFY_CODE_LOGIN_${captchaId}`);
  if (!code) {
    throw new Error(`验证码不存在或已过期 (captchaId=${captchaId})`);
  }
  return code;
}