gs-gephi
========

An interconnection project between Gephi and GraphStream based on the `Graph Streaming API`_. This project is distributed under MIT license within a LICENSE file in it.

Install
-----------

Follow the steps to install this project.

1) Fork and checkout the latest version of this repository: 
::
  git clone git@github.com:graphstream/gs-gephi.git
2) build the project use Maven:
::
  cd gs-gephi
  mvn install
3) generate eclipse project files:
::
  mvn eclipse:eclipse
4) go to Eclipse and select "import" in the file menu. Choose "Existing projects into workspace" and select your project directory.
::

5) Once your workspace in configured using the previous command, you can directly add a maven artifact in the build path of your eclipse project. First, right-click on your eclipse project and go the build path configuration. Then click on "Add Variable" in the "Librairies" tab.Select the "M2_REPO" variable and click on "Extend". Final step is to select the artifact you want to use. 
::

You can check the `manual`_ to see the detailed discription and tutorials showing how to use it.
 
Ongoing work...

.. _Graph Streaming API: http://wiki.gephi.org/index.php/Specification_-_GSoC_Graph_Streaming_API
.. _manual: https://github.com/graphstream/gs-gephi/wiki/JSONStream-Manual