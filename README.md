# Project README
Follow the steps below to compile, run, and test the Hadoop program:this instruction assumes you have setup the Hadoop environment variable to access the hadoop commands or else you need to cd to the hadoop directory


# Compile and Run the Program

Follow the steps below to compile, run, and test the Hadoop program:

## 1. Compile the Program
If the program is not compiled or you want to recompile it, execute the following commands:
```bash
hadoop com.sun.tools.javac.Main WordCount.java
jar cf wc.jar WordCount*.class
```

## 2. Start the DataNode
Before running the program, ensure the Hadoop DataNode is running. Use the following command:
```bash
start-dfs.sh
```

## 3. Delete Existing Output Directory
If you are running the program multiple times (with or without stopwords), delete the existing output directory to avoid the "File already exists" exception. Run the following:
```bash
hdfs dfs -rm -r /user/{user_name}/wordcount/output
```
**Note:** Replace `{user_name}` with your actual username.

## 4. Run the MapReduce Program Without Stopwords
After starting the DataNode (refer to Step 2), execute the program without stopwords:
```bash
hadoop jar wc.jar WordCount /user/{user_name}/wordcount/input /user/{user_name}/wordcount/output/
```

## 5. View the Output of the MapReduce Program
To check the results of the MapReduce job, use the following command to view the output:
```bash
hadoop fs -cat /user/{user_name}/wordcount/output/part-r-00000
```

## 6. Run the MapReduce Program With Stopwords
To run the program using a stopwords file:
1. Ensure the DataNode is running (refer to Step 2).
2. Execute the program with the path to the stopwords file:
```bash
hadoop jar wc.jar WordCount /user/{user_name}/wordcount/input /user/{user_name}/wordcount/output/ ~/path/to/stop_words.txt
```
**Note:** Replace `~/path/to/stop_words.txt` with the actual path to your stopwords file.

## 7. After completing stop datanodes
```bash
   stop-dfs.sh     // stop datanode
```
# Helpfull commands 
### stopword used 
  - https://github.com/stopwords-iso/stopwords-en/blob/master/stopwords-en.txt
### version
  - hadoop version
### add to .bashrc assuming JAVA_HOME is setup
   export HADOOP_CLASSPATH=${JAVA_HOME}/lib/tools.jar
### compile 
  - hadoop com.sun.tools.javac.Main WordCount.java
  - jar cf wc.jar WordCount*.class
### create input and output directory 
  hdfs dfs -mkdir -p /user/ai-server-02/wordcount/input
  hdfs dfs -mkdir -p /user/ai-server-02/wordcount/output
### move files to input 
  hdfs dfs -put /home/ai-server-02/code_repo/dsai_a4/books/*  /user/ai-server-02/wordcount/input
### delete  
   hdfs dfs -rm -r /user/ai-server-02/wordcount
   hdfs dfs -rm -r /user/ai-server-02/wordcount/input
   hdfs dfs -rm -r /user/ai-server-02/wordcount/output
### run mapreduce program 
 - [without removing stop words]
    - hadoop jar wordcount.jar WordCount /path/to/input /path/to/output 
 - [removing stop words]
   - hadoop jar wordcount.jar WordCount /path/to/input /path/to/output /path/to/stopwords.txt
### run 
  hadoop jar wc.jar WordCount /user/ai-server-02/wordcount/input /user/ai-server-02/wordcount/output/
### show output 
  hadoop fs -cat /user/ai-server-02/wordcount/output/part-r-00000
### starting and stoping and logs datanodes
  - start-dfs.sh    // start datanode
  - stop-dfs.sh     // stop datanode
  - jps    // check if datanodes started
  - cat $HADOOP_HOME/logs/hadoop-*-namenode-*.log  // checking the log
  - hdfs namenode -format   // if error in file format,the error is webbrowser for node does not display ,this will reformat it but it will wipe out the existing data 