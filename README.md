# Virtual-Tutor
This repository contains the source files for the Virtual Tutor. It is organized as follows:

## The 'assistant'-folder
This folder contains an electron-project of the User Interface part for the Virtual Tutor. In order to run it you will need node.js and npm installed.

_More information on how to run the code will be provided shortly._

## The 'plugin'-folder
This folder contains an IntelliJ-Project. It contains the source files for the IntelliJ-Plugin. Please note that this project needs to be configured as a plugin-project after opening in IntelliJ. Otherwise it can not be started or tested from IntelliJ.

_More information on how to run the code will be provided shortly._

## The 'logReader'-folder
In this folder you will find the python-scripts used to extract information out of the logfiles created by the IntelliJ-Plugin. This is not part of the Virtual Assistant but may be of use as a starting point for future scientific analysis of programming behaviour.

These scripts can be used as a starting point for your own scripts. There is nothing to install here but please consider that the scripts rely on certain file types being in certain folders and may not be of any use as they are. Until the authors of this repository conduct further analysis of their own there will be no improvements made on the scripts.

# Complete package of a runnable version of the assistant

A working version of the Virtual Tutor (including a Demo project) can be downloaded here: https://github.com/Software-Engineering-Education-Tools/Virtual-Tutor/releases/tag/1.0. Once you unpack the zip-file you will find a portable version of IntelliJ with the plugin already installed. All you need to do, is to click on the 'idea'-shortcut at the top-most level in the folder.

**Please note:** Currently there is only a windows version of the Virtual Assistant available.
