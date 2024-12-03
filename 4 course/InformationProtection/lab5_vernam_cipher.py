import random

def generate_key(length):
    return ''.join(chr(random.randint(0, 255)) for _ in range(length))

def vernam_cipher(text, key):
    return ''.join(chr(ord(t) ^ ord(k)) for t, k in zip(text, key))

def check(original_text, decrypted_text):
    if decrypted_text == original_text:
        print("Успешно: текст совпадает с исходным!")
    else:
        print("Ошибка: текст не совпадает с исходным.")

def main():
    text = input("Введите текст для шифрования: ")
    
    key = generate_key(len(text))
    print(f"Случайный ключ: {key}")
    
    encrypted_text = vernam_cipher(text, key)
    print(f"Зашифрованный текст: {encrypted_text}")
    
    decrypted_text = vernam_cipher(encrypted_text, key)
    print(f"Расшифрованный текст: {decrypted_text}")
    
    check(text, decrypted_text)

if __name__ == "__main__":
    main()
