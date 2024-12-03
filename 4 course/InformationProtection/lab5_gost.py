from Crypto.Cipher import AES
from Crypto.Random import get_random_bytes
import hashlib

def pad(data):
    padding_len = 16 - len(data) % 16
    return data + bytes([padding_len]) * padding_len

def unpad(data):
    padding_len = data[-1]
    return data[:-padding_len]

def encrypt_file(input_file, output_file, key):
    cipher = AES.new(key, AES.MODE_CBC)  # используем режим CBC (Cipher Block Chaining)
    iv = cipher.iv  # вектор инициализации

    with open(input_file, 'rb') as f_in:
        plaintext = f_in.read()
    plaintext = pad(plaintext)  # выравниваем данные

    ciphertext = cipher.encrypt(plaintext)

    with open(output_file, 'wb') as f_out:
        f_out.write(iv)  # сначала записываем IV
        f_out.write(ciphertext)

def decrypt_file(input_file, output_file, key):
    with open(input_file, 'rb') as f_in:
        iv = f_in.read(16)  # читаем IV (первые 16 байт)
        ciphertext = f_in.read()

    cipher = AES.new(key, AES.MODE_CBC, iv=iv)

    plaintext = cipher.decrypt(ciphertext)
    plaintext = unpad(plaintext)  # убираем дополнение

    with open(output_file, 'wb') as f_out:
        f_out.write(plaintext)

# хэш-функция для файла на основе sha256
def hash_file(file_path):
    hasher = hashlib.sha256()
    with open(file_path, 'rb') as f:
        while chunk := f.read(8192):
            hasher.update(chunk)
    return hasher.hexdigest()

def test_law_effect(file_path):
    original_hash = hash_file(file_path)

    # создаем копию файла с изменением одного байта
    modified_file = 'modified_input.txt'
    with open(file_path, 'rb') as f_in, open(modified_file, 'wb') as f_out:
        data = f_in.read()
        data = bytearray(data)
        data[0] ^= 0xFF  # инвертируем первый байт
        f_out.write(data)

    modified_hash = hash_file(modified_file)

    print(f"Оригинальный хеш: {original_hash}")
    print(f"Измененный хеш:   {modified_hash}")

    diff = sum(1 for a, b in zip(original_hash, modified_hash) if a != b)
    print(f"Различий в хеше: {diff} символов")

if __name__ == "__main__":
    key = get_random_bytes(16)  # генерируем ключ длиной 16 байт 
    encrypt_file('input.txt', 'encrypted.bin', key)
    decrypt_file('encrypted.bin', 'decrypted.txt', key)
    print("Файл зашифрован и расшифрован!")
    test_law_effect('input.txt')
