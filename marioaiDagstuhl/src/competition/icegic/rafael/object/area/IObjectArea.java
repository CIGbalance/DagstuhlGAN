package competition.icegic.rafael.object.area;


public interface IObjectArea
{

	public long getX();

	public long getY();


	public boolean isPaused();

	public void setEnvironment(float[] observation);

	public boolean isObjectComing(IObjectArea object);

}
