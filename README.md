# Тестовое задание
## Разработка
https://github.com/zvdenis/Development/
Программа состоит из 6 классов
* Application - отвечает за консольный ввод/вывод и определение файлов для разбора
* Parser - отвечает за чтение и разбор файла по строкам, и создает LogElement
* LogCounter - отвечает за анализ полученных LogElement (формирует требуемые данные)
* DataWriter - отвечает за вывод требуемых данных
* DataPart (POJO)
* LogElement (POJO)

Во время анализа объемных данных узким местом являются процессы чтения и вывода в файл, поэтому эти задачи выведены в отдельные потоки.

### Application
* public static void main(String[] args) - точка входа
* public static void parseFiles(File[] listOfFiles) - разбирает список файлов


### Parser
* public void parseFile(File file, LogCounter logCounter) - анализирует один файл
* public void parseLine(String line) - разбирает строку и создает LogElement для анализа


### LogCounter
* public void addLogElement(LogElement logElement) - добавляет элемент в очередь анализа
* private void processLogElement(LogElement logElement) - анализирует элемент, если уже есть элемент с таким ID создает DataPart, в противном случае добавляет в словарь
* public void addToData(DataPart dataPart) - добавляет DataPart в соответствующую временную "корзину"
* public void dataChecker() - проверяет очередь аналзиа и словарь с обработанными данными
* private void printBucket(LocalTime time) - формирует данные для вывода и передает в DataWriter
* private LocalTime getDataMinTime() - находит корзину с минимальным временем


### DataWriter
* public void addToQueue(LocalTime time, String type, ArrayList<Long> completionTimes) - добавляет данные в очередь вывода
* private void printType(LocalTime time, String type, ArrayList<Long> completionTimes) - выводит строку с полученное информацией
* private void processQueue() - выводит в файл пока очередь не пустая, в противном случае поток блокируется
