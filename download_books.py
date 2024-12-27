import requests
import re
import os

def ensure_books_directory_exists():
    # Check if "books" directory exists, if not, create it
    if not os.path.exists("books"):
        os.makedirs("books")
        print("Directory 'books' created.")
    else:
        print("Directory 'books' already exists.")

def sanitize_filename(title):
    # Replace spaces with underscores and remove special characters
    sanitized_title = re.sub(r'[^\w\s]', '', title)  # Remove special characters except spaces
    sanitized_title = re.sub(r'\s+', '_', sanitized_title)  # Replace spaces with underscores
    return sanitized_title

def download_first_n_english_books(n):
    base_url = "https://www.gutenberg.org/cache/epub/{}/pg{}.txt"
    downloaded_count = 0
    book_id = 1  # Start from ID 1

    # Ensure the "books" directory exists
    os.makedirs("books", exist_ok=True)

    while downloaded_count < n:
        url = base_url.format(book_id, book_id)
        
        try:
            # Send a GET request to download the file
            response = requests.get(url)
            response.raise_for_status()  # Raise an error for HTTP issues
            
            # Check if the file size is greater than 100k
            if len(response.content) < 100_000:
                print(f"Skipped ID {book_id}. File size less than 100k.")
                book_id += 1
                continue
            
            # Extract the text content
            content = response.text
            
            # Check if the book is in English
            language_match = re.search(r"Language:\s*(.+)", content)
            if language_match and "English" in language_match.group(1):
                # Extract the title using regex
                title_match = re.search(r"Title:\s*(.+)", content)
                if title_match:
                    title = title_match.group(1).strip()
                    print(f"Extracted Title: {title} (English)")
                    
                    # Sanitize the title for filenames
                    sanitized_title = sanitize_filename(title)
                    
                    # Create a filename using the title and ID
                    file_name = f"books/{sanitized_title[:50]}_{book_id}.txt"  # Save inside "books" directory
                    
                    # Save the content
                    with open(file_name, "w", encoding="utf-8") as file:
                        file.write(content)
                    
                    print(f"Downloaded and saved as: {file_name}")
                    downloaded_count += 1
                else:
                    print(f"No title found for ID {book_id}. Skipping.")
            else:
                print(f"Skipped ID {book_id}. Not an English book.")
        except requests.exceptions.RequestException as e:
            print(f"Failed to download ID {book_id}. Error: {e}")
        
        # Increment the book ID for the next iteration
        book_id += 1

if __name__ == "__main__":
    # Ensure the "books" directory exists
    ensure_books_directory_exists()
    # Specify the number of English books to download
    num_books = int(input("Enter the number of English books to download: "))
    download_first_n_english_books(num_books)
